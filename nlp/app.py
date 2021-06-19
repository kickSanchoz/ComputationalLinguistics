import datetime
import json

from bson import ObjectId
from flask import Flask, request, jsonify, abort
from flask.json import JSONEncoder
from flask_pymongo import PyMongo
from starlette.config import Config
from starlette.datastructures import Secret

from apscheduler.schedulers.background import BackgroundScheduler

from modules.Word2Vec import MyWord2Vec
from modules.parser import parse_news
from modules.tonality import ModelTonality

# Загрузка данных их файла
config = Config(".env")
MONGO_USER = config("MONGO_USER", cast=str)
MONGO_PASSWORD = config("MONGO_PASSWORD", cast=Secret)
MONGO_SERVER = config("MONGO_SERVER", cast=str, default="db")
MONGO_PORT = config("MONGO_PORT", cast=str, default="27017")

# Словарь типов времени
type_time = ["min", "hours"]


def log(msg: str) -> None:
    print(datetime.datetime.now().strftime("%H:%M:%S") + ": " + msg)


def get_mongo_url(db: str) -> str:
    # Формируется строка для подключения к бд
    return f"mongodb://{MONGO_USER}:{MONGO_PASSWORD}@{MONGO_SERVER}:{MONGO_PORT}/{db}?authSource=admin"


class MyJSONEncoder(JSONEncoder):
    # Вспомогательный класс перевода данных в JSON. Добавляет знания о классе ObjectId
    def default(self, o):
        if isinstance(o, ObjectId):
            return str(o)
        return json.JSONEncoder.default(self, o)


def get_application():
    # Формируем бэкграунд для выполения задач по расписанию
    scheduler = BackgroundScheduler()
    # Создаем приложение backend'a
    app = Flask(__name__)
    app.json_encoder = MyJSONEncoder
    # Подключает БД к приложению
    mongo = PyMongo(app, get_mongo_url("censored"))
    # Подключение вспомогатльных модулей
    w2v = MyWord2Vec(mongo.db)
    mt = ModelTonality(mongo.db)

    # Формируем задачу для выполнения в фоне
    def start_parse():
        # Используем краулер для добавления новостей
        parse_news(mongo.db)
        # Расчитываем тональность предложений
        mt.predict_tonality()

    # Добавляем задачу в расписание и запускаем
    task = scheduler.add_job(func=start_parse, trigger="interval", minutes=5)
    scheduler.start()

    @app.get("/news")
    def send_news():
        # Получаем значения из запроса
        count_news = int(request.args.get('count_news'))
        skip_news = int(request.args.get('skip_news'))

        # Формируем сортировку по убыванию даты
        sort = list({
                        'date': -1
                    }.items())
        
        # Получаем данные из БД
        result = mongo.db.news.find(
            filter={},
            sort=sort,
            skip=skip_news,
            limit=count_news
        )
	# Соединяем строки в единый текст
        send = []
        for i in result:
            obj = i
            obj['text'] = " ".join(obj["text"])
            send.append(obj)
        # Возвращаем значения
        return jsonify(send)

    @app.post("/parse_timer")
    def change_timer():
	# Получаем значения
        interval = int(request.args.get('interval'))
        type = request.args.get('type')
	
	# Если тип не совпадает со словарем, возвращаем ошибку
        if type not in type_time:
            abort(409)
        if type == "min":
	    # Если указанные минуты не в интервале, выдаем ошибку
            if interval < 2 or interval > 59:
                abort(409)
	    # Иначе задаем новый интервал для обновления
            task.reschedule(trigger='interval', minutes=interval)
	# Аналогично для часов
        if type == "hour":
            if interval < 0 or interval > 59:
                abort(409)
            task.reschedule(trigger='interval', hours=interval)
        return "OK"

    @app.get("/neighbour_synonymous")
    def get_ne_sy():
	# Получаем значение
        word = request.args.get('word')
	# Если слова нет в модели, возвращаем ошибку
        if not w2v.hasWord(word):
            abort(400)
	# Иначе возвращаем синонимы и рядом стоящие слова
        return jsonify({
            "synonyms": w2v.findSynonimus(word),
            "neighbour": w2v.findNeighbour(word)
        })

    @app.get("/tonality")
    def get_tonality():
	# Получаем значения из бд и возвращаем пользователю
        res = mongo.db.tonality.find()
        return jsonify([o for o in res])

    return app


app = get_application()

if __name__ == '__main__':
    app.run(host="0.0.0.0")
