from geventwebsocket import logging

from app import app
from gevent.pywsgi import WSGIServer
from geventwebsocket.handler import WebSocketHandler


if __name__ == '__main__':
    # app.run(host='192.168.1.6', debug=True)
    server = WSGIServer(('192.168.1.6', 5000), app, handler_class=WebSocketHandler)
    server.serve_forever()
