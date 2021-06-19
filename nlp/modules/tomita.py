import subprocess
from typing import Optional, OrderedDict, Tuple
import xmltodict
import os

import pymongo
from pymongo.database import Database


class TomitaParser:

    def __init__(self, db, path_to_tomita: str):
        self._db = db
        self._path_to_tomita = path_to_tomita
        self._isConsole = False
	# Копируем файлы в директорию томиты
        os.system("cp -u ./tomita_conf/* "+path_to_tomita+"/")
	# Размещаем словари в директорию томиты
        self._load_fio()
        self._load_post()
        self._load_landmark()
        self._load_gubernator()

    def _parse_xml(self, xml: str) -> Optional[dict]:
	# Переводим из xml в формат словарей Питона
        data = xmltodict.parse(xml)
	# Если фактов не обнаружено, то возвращаем ничего
        if data["fdo_objects"] is None:
            return None
        build = list()
        person = list()
        res = dict()
	# Вытаскиваем факты
        facts = data["fdo_objects"]["document"]["facts"]["Fact"]
	# Для каждого факта
        for cur in facts:
	    # если факт один, то работаем с этим фактом, иначе работает с каждым фактом
            elem = cur if isinstance(cur, OrderedDict) else facts
	    # Если факт - здание, то добавляем в массив найденных значений здание
            if "building" in elem:
                build.append({"name": elem["building"]["@val"], "pos": int(elem["building"]["@pos"]),
                              "len": int(elem["building"]["@len"])})
	    # Аналогично для персон
            if "person" in elem:
                person.append({"name": elem["person"]["@val"], "pos": int(elem["person"]["@pos"]),
                               "len": int(elem["person"]["@len"])})
            # Если работа была с одним фактом, то завершаем
            if elem == facts:
                break
        # Возвращаем факты если они присутствуют
        if len(build):
            res["building"] = build
        if len(person):
            res["person"] = person
        return res

    def _load_gubernator(self):
	# Достаем из БД значения Губернатора и записываем в файл
        guber = self._db.dict.find_one(
            filter={"type": "gubernator"}
        )["values"]
        with open(self._path_to_tomita + "/gubernator_1mw", "w") as file:
            for string in guber:
                file.write(string + "\n")

    def _load_fio(self):
	# Достаем из БД ФИО персон
        FIO = self._db.dict.find_one(
            filter={"type": "FullName"}
        )['values']
	# Сохраняем в файл
        with open(self._path_to_tomita + "/fio", "w") as file:
            for string in FIO:
                file.write(string + "\n")
	# генерируем и сохраняем в файл комбинации ФаимилияИмя и ИмяФамилия
        with open(self._path_to_tomita + "/fi", "w") as file_fi:
            with open(self._path_to_tomita + "/if", "w") as file_if:
                for string in FIO:
                    words = string.split(" ")
                    file_fi.write(words[0] + " " + words[1] + "\n")
                    file_if.write(words[1] + " " + words[0] + "\n")

    def _load_post(self):
	# Достаем из бд все варианты должности и сохраняем в файл (Для каждого номер главного слова свой файл)
        for i in range(1, 5):
            name = f"post_{str(i)}"
            values = self._db.dict.find_one(
                filter={"type": "post", "MainWord": i}
            )['values']
            with open(self._path_to_tomita + "/" + name + "mw", "w") as file:
                for string in values:
                    file.write(string + "\n")

    def _load_landmark(self):
	# Аналогично с достопремичательностями
        for i in range(1, 5):
            name = f"dost_{str(i)}"
            values = self._db.dict.find_one(
                filter={"type": "landmark", "MainWord": i}
            )['values']
            with open(self._path_to_tomita + "/" + name + "mw", "w") as file:
                for string in values:
                    file.write(string + "\n")
        name = "dost_const"
        values = self._db.dict.find_one(
            filter={"type": "landmark", "isConst": True}
        )['values']
        with open(self._path_to_tomita + "/" + name, "w") as file:
            for string in values:
                file.write(string + "\n")

    def check_citation(self, sentence: str) -> Tuple[str, Optional[dict]]:
	# Запускаем Томита парсер и получаем результат  работы
        p = subprocess.run(['./tomita-parser', 'config.proto'], stdout=subprocess.PIPE,
                           input=sentence, encoding='utf-8', cwd=self._path_to_tomita)
        if p.returncode:
            raise SystemError  # todo change Exception
	# Получаем факты из работы парсера
        res = self._parse_xml(p.stdout)
        if self._isConsole:
            print(sentence)
            print(res)
        new_text = sentence
	# Если имеются факты
        if res is not None:
	    # Для каждого факта
            for key, value in res.items():
		# Для каждого элемента факта
                for cur_val in value:   
		    # Если найдена персона
                    if key == "person":
			# Заменяем порядок ФИО на ФамилияИмя
                        replace_text = cur_val["name"].split(" ")
                        if self._db.dict.find_one(
                                filter={"values": {"$regex": cur_val["name"].split(" ")[0]}, "type": "FullName"}
                        ) is not None:
                            replace_text = replace_text[0] + replace_text[1]
                        elif len(replace_text) != 1:
                            replace_text = replace_text[1] + replace_text[0]
                        else:
                            replace_text = replace_text[0]
                    else:
			# Иначе убираем пробелы
                        replace_text = cur_val["name"].replace(" ", "")
		    # Находим старое слово, которое надо заменить
                    pos = cur_val["pos"]
                    pos = pos if pos > 0 else 0
                    pos_end = pos + cur_val["len"]
		    # Заменяем в предложении найденый факт на нормализированное значение
                    new_text = new_text.replace(sentence[pos:pos_end], replace_text)

        return new_text, res

    def setConsole(self, state: bool):
        self._isConsole = state


if __name__ == '__main__':
    """
    Для запуска установите рабочую директорию на каталог выше!
    """
    # path_to_tomita = "/home/aioki/Документы/tomita-parser/build/bin"
    # conn_str = "mongodb://localhost/"
    # client = pymongo.MongoClient(conn_str, serverSelectionTimeoutMS=5000)
    # db: Database = client.censored
    #
    # pars = TomitaParser(db, path_to_tomita)
    # # vals = db.news.find({}, {"_id": 1, "text": 1})
    # # for news in vals:
    # #     _id = news["_id"]
    # #     new_text: list = news["text"]
    # #     for i in range(len(new_text)):
    # #         new_sentence, cit = pars.check_citation(new_text[i])
    # #         if cit is not None:
    # #             new_text[i] = new_sentence
    # #             db.citiation.insert_one({"news_id": news["_id"], "sentence": new_sentence, "cit": cit})
    # #
    # #     db.new_news.update_one({"_id": _id}, {"$set": news})
    #
    # text = "На площадке XXIV Петербургского международного экономического форума глава города Волгограда Андрей Бочаров и президент ПАО «Ростелеком» Михаил Осеевский подписали соглашение о сотрудничестве. Также Олег Савченко пришел на фан встречу."
    # # text = "Челябинского колхозника"
    # pars.setConsole(True)
    # val, _ = pars.check_citation(text)
    # print("=" * 80)
    # print(val)
