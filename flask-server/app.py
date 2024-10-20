from flask import Flask, render_template
from flask_socketio import SocketIO, emit

app = Flask(__name__)
app.config['SECRET_KEY'] = 'your_secret_key'  # Replace with your secret key
socketio = SocketIO(app, cors_allowed_origins='*')

@app.route('/')
def index():
    return render_template('index.html')

@socketio.on('connect')
def handle_connect():
    print('Web client connected')

@socketio.on('send_message')
def handle_send_message(data):
    message = data['message']
    print(f"Received message: {message}")
    # Emit the message to all connected Android clients
    emit('new_notification', {'message': message}, broadcast=True)

if __name__ == '__main__':
    # Paths to your certificate and key files
    # cert_path = 'cert.pem'
    # key_path = 'key.pem'
    socketio.run(app, 
                 host='0.0.0.0', port=5000, 
                 debug=True)
                 #, ssl_context=(cert_path, key_path)
