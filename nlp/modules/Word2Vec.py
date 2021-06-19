import re

import pymongo
from py4j.protocol import Py4JJavaError
from pymongo.database import Database
from pyspark.mllib.feature import Word2VecModel
from pyspark.sql import SparkSession, DataFrame
from pyspark.ml.feature import Word2Vec, StopWordsRemover
import string as _string


# Убираем лишние символы

def remove_punctuation(text):
    return text.translate(str.maketrans('', '', _string.punctuation + "»"))


def remove_linebreaks(text):
    return text.strip()


def get_only_words(tokens):
    return list(filter(lambda x: re.match('[a-zA-Zа-яёА-ЯЁ]+', x), tokens))


class MyWord2Vec:

    def __init__(self, db):
        self._db = db
	# Запускаем Spark сессию
        self.spark = SparkSession \
            .builder \
            .appName("SimpleApp") \
            .getOrCreate()
	# Обучаем модель
        self._model = self._fit_model()

    def _fit_model(self):

        text = []
        # Получаем список предложений из бд
        vals = self._db.new_news.find({}, {"_id": 1, "text": 1})
	# Соединяем массивы предложений в один большой массив
        for news in vals:
            for sentence in news["text"]:
                text.append(sentence)
        # разбиваем на слова и убираем лишние символы
        split_text = [o.split(" ") for o in text]
        clear_text = []
        for sentence in split_text:
            clear_text.append(get_only_words(remove_punctuation(remove_linebreaks(_word)) for _word in sentence))
        # Создаем DataFrame
        new_doc = self.spark.createDataFrame([(o,) for o in clear_text], ["words"])
        # new_doc.show()
	# Убираем стоп слова
        swr = StopWordsRemover.loadDefaultStopWords("russian")
        remover = StopWordsRemover(inputCol='words', outputCol='filtered', stopWords=swr)
        self._filtered = remover.transform(new_doc)
        # filtered.show(vertical=True, truncate=False)
	# создаем модель и обучаем
        word2Vec = Word2Vec(vectorSize=50, minCount=3, inputCol='filtered', outputCol='result')
        model: Word2VecModel = word2Vec.fit(self._filtered)
        return model
        # model.write().overwrite().save("./w2v")

    def hasWord(self, word: str):
	# Получаем список слов в наборе
        tokens = self._filtered.select("filtered").collect()
	# для каждой строки
        for row in tokens:
	    # Получаем набор слов
            lst: list = row[0]
	    # Если слово присутствует в строке, то возвращаем истину, иначе ложь
           if word in lst:
                return True
        return False

    def findSynonimus(self, word: str):
	# Запрашиваем у модели 5 синонимов для слова
        res: DataFrame = self._model.findSynonyms(word, 5)
	# Создаем массив полученных слов
        val = res.collect()
        ret = list()
        for word, count in val:
            ret.append(word)
        return ret
        # synon = [o[] for o in res.collect()]

    def findNeighbour(self, word: str):
	# Запрашиваем все токены
        tokens = self._filtered.select("filtered").collect()	
	# Ищем слово в каждой строке	
        neighbour = set()
        for row in tokens:
            lst: list = row[0]
	    # если слово присутствует в наборе
            if word in lst:
		# Получаем индекс слова в набрре
                pos = lst.index(word)
		$# Добавляемм соседние слова
                if pos > 0:
                    neighbour.add(lst[pos - 1])
                if pos < len(lst) - 1:
                    neighbour.add(lst[pos + 1])
        return list(neighbour)

    def __del__(self):
        self.spark.stop()


# # model.getVectors().show()
# # model.save(spark, "./w2v.bin")
# find = "БОЧАРОВАНДРЕЙ"
# try:
#     synon: DataFrame = model.findSynonyms(find, 20)
#     synon.show(vertical=True, truncate=False)
#     val = synon.first()[0]
#     print(val)
# except Py4JJavaError as e:
#     print("No value")
#
# tokens = filtered.select("filtered").collect()
# neighbour = set()
# for row in tokens:
#     lst: list = row[0]
#     if find in lst:
#         pos = lst.index(find)
#         if pos > 0:
#             neighbour.add(lst[pos - 1])
#         if pos < len(lst) - 1:
#             neighbour.add(lst[pos + 1])
# print("=" * 80)
# for i in neighbour:
#     print(i)
# print("=" * 80)


if __name__ == '__main__':
    """
    
    NEED CHANGE WORKDIR
    """
    conn_str = "mongodb://localhost/"
    client = pymongo.MongoClient(conn_str, serverSelectionTimeoutMS=5000)
    db: Database = client.censored
    w2v = MyWord2Vec(db)
    # w2v.fit_model(db)
    wrod = "SadASDa"  # БОЧАРОВАНДРЕЙ
    if w2v.hasWord(wrod):
        print(str(w2v.findSynonimus(wrod)))
        print(str(w2v.findNeighbour(wrod)))
    else:
        print("err")
