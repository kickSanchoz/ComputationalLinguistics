from pymongo import MongoClient
from dostoevsky.tokenization import RegexTokenizer
from dostoevsky.models import FastTextSocialNetworkModel


class ModelTonality:

    def __init__(self, db):
        self._tokenizer = RegexTokenizer()
        self._model = FastTextSocialNetworkModel(tokenizer=self._tokenizer)
        self._db = db

    def predict_tonality(self):
        data = self._db.citiation.find()

        lendata = data.count()
        sentencelist = []
        idlist = []

        for k in range(lendata - 1):
            sentencelist.append(data[k]['sentence'])
            idlist.append(data[k]['_id'])

        results = self._model.predict(sentencelist, k=2)
        self._db.tonality.remove({})
        for message, sentiment, idd in zip(sentencelist, results, idlist):
            self._db.tonality.insert_one({"_id": idd, "result": sentiment, "sentence": message})


if __name__ == '__main__':
    client = MongoClient("mongodb://censored:censored@censored:27017")
    db = client.nlp
    mt = ModelTonality(db)
    mt.predict_tonality()
