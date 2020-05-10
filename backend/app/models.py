from app import db


class User(db.Model):
    __tablename__ = 'user'
    id = db.Column(db.Integer, primary_key=True)
    phone = db.Column(db.Integer, unique=True)
    password = db.Column(db.String(32))
    nickname = db.Column(db.String(18), nullable=True)
    avatar = db.Column(db.String(256), nullable=True)
    signature = db.Column(db.Text, nullable=True)
    score = db.Column(db.Float, default=0.0)

    def __repr__(self):
        return '<User id:{} name:{}>'.format(self.id, self.nickname)
