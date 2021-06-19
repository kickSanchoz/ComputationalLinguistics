import datetime
from nltk import tokenize
import pymongo
import requests
import ast
from bs4 import BeautifulSoup, Tag, ResultSet
from pymongo.database import Database
from starlette.config import Config

from modules.tomita import TomitaParser

# Загрузка переменных и установка значений
config = Config(".env")
path_to_tomita = config("PATH_TO_TOMITA", cast=str)
domain = "https://www.volgograd.kp.ru"
news_url = domain + "/online/news/"


def log(msg: str) -> None:
    print(datetime.datetime.now().strftime("%H:%M:%S") + ": " + msg)


def parse_news(db):
    log("Start parse")
    # Подготавливаем томиту парсер
    tomita = TomitaParser(db, path_to_tomita)
    # определяем текущий месяц и год
    year = datetime.datetime.now().year
    month = datetime.datetime.now().month
    # Загружаем через апи данные по новостям
    res = requests.get(
        domain + '/content/api/1/pages/get.json/result/?pages.age.month={0}&pages.age.year={1}&pages.direction=last&pages.target.class=100&pages.target.id=5'.format(
            month, year))
    # Конвертируем в объекты Python
    data = ast.literal_eval(res.content.decode("UTF-8"))
    # Для каждой страницы с новостями
    count_pages = data["meta"][1]['value']
    for page in range(count_pages, 0, -1):
        # Загружаем список новостей с указанной страницы
        res = requests.get(
            domain + "/content/api/1/pages/get.json/result/?pages.age.month={0}&pages.age.year={1}&pages.direction=page&pages.target.class=100&pages.target.id=5&pages.number={2}".format(
                month, year, page)
        )
        # Переменные для хранения новостей
        parse_news = []
        tomita_news = []
        # Конвертируем в объекты Python
        data = ast.literal_eval(res.content.decode("UTF-8"))
        # Для каждой новости
        for news in data['childs']:
            # Если такой новости нет в БД
            if db.news.find_one({"_id": news["@id"]}) is None:
                # Загружаем страницу с текущей новостью
                url = news_url + str(news["@id"])
                r = requests.get(url)
                # Находим блок со статьей
                soup = BeautifulSoup(r.text, features="html.parser")
                news_block: Tag = soup.find('div',
                                            {
                                                'class': "styled__Content-sc-1wayp1z-0 styled__ContentBody-sc-1wayp1z-5 dIXOfC gELcmL"})
                # Вытаскиваем url картинок
                image_tag: ResultSet = news_block.find_all('img')
                image = set()
                for i in image_tag:
                    img_url: str = i.get('src')
                    if img_url.startswith("//"):
                        img_url = "https:" + url
                    image.add(img_url)
                # Вытаскиваем текст новости
                text_tag: ResultSet = news_block.find_all("p", {"class": "styled__Paragraph-sc-1wayp1z-16 hVxcah"})
                text = str()
                for i in text_tag:
                    text += i.text + " "
                # Разбиваем на предложения
                text = tokenize.sent_tokenize(text, language="russian")
                # Для каждого предложения в тексте
                tomita_text = text.copy()
                for i in range(len(text)):
                    # Проверяем упоминание с помощью Томиты парсера
                    new_sentence, cit = tomita.check_citation(text[i])
                    # Если присутствует упоминание
                    if cit is not None:
                        # Заменяем предложение на предложение с униграммами
                        tomita_text[i] = new_sentence
                        # Сохраняем в БД упоминание и тип
                        db.citiation.insert_one({"news_id": news["@id"], "sentence": new_sentence, "cit": cit})
                # Сохраняем оригинальную новость
                parse_news.append({
                    "_id": news["@id"],
                    "url": url,
                    "tags": news["@tag"],
                    "title": news["ru"]["title"],
                    "text": text,
                    "url_image": list(image),
                    "date": news["meta"][0]["value"],
                    "comment_count": news["meta"][5]['value'],
                })
                # Сохраняем обработанную новость
                tomita_news.append({
                    "_id": news["@id"],
                    "url": url,
                    "tags": news["@tag"],
                    "title": news["ru"]["title"],
                    "text": tomita_text,
                    "url_image": list(image),
                    "date": news["meta"][0]["value"],
                    "comment_count": news["meta"][5]['value'],
                })
        # Добавляем информацию в БД
        for news in parse_news:
            if db.news.find_one({"_id": news["_id"]}) is not None:
                db.news.update_one({"_id": news["_id"]}, {"$set": news})
            else:
                db.news.insert_one(news)
        for news in tomita_news:
            if db.new_news.find_one({"_id": news["_id"]}) is not None:
                db.new_news.update_one({"_id": news["_id"]}, {"$set": news})
            else:
                db.new_news.insert_one(news)
    log("Stop parse")


if __name__ == '__main__':
    conn_str = "mongodb://localhost/"
    client = pymongo.MongoClient(conn_str, serverSelectionTimeoutMS=5000)
    db: Database = client.censored
    parse_news(db)
