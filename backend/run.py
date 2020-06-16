from app import app
from gevent.pywsgi import WSGIServer
from  geventwebsocket.handler import WebSocketHandler


if __name__ == '__main__':
    # app.run(host='127.0.0.1', debug=True)
    server = WSGIServer(('0.0.0.0', '5000'), app, handler_class=WebSocketHandler)
    server.serve_forever()
