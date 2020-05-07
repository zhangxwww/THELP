from app import db


class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    phone = db.Column(db.String(11))
    password = db.Column(db.String(128))
    nickname = db.Column(db.String(18))
    avatar = db.Column(db.String(256))
    signature = db.Column(db.Text)
    score = db.Column(db.Float)

    def __repr__(self):
        return '<User id:{} name:{}>'.format(self.id, self.nickname)
