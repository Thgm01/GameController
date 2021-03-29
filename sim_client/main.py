#!/usr/bin/env python3

import socket
import time

HOST = '127.0.0.1'  # The server's hostname or IP address
PORT = 8750        # The port used by the server


with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
    s.connect((HOST, PORT))

    start = time.time()
    ready, set, play, penalized, scored, kickoff = False, False, False, False, False, False

    message_numb = 0

    while True:
        delta = time.time() - start

        clock_data = f'{message_numb}:CLOCK:{int(delta * 3 * 1000)}\n'
        print(clock_data)

        s.sendall(clock_data.encode('ascii'))

        if delta > 12 and not kickoff:
            kickoff = True
            s.sendall(f"{message_numb}:SIDE_LEFT:26\n".encode('ascii'))

        if delta > 3 and not ready:
            ready = True
            s.sendall(f"{message_numb}:STATE:READY\n".encode('ascii'))

        if delta > 7 and not set:
            set = True
            s.sendall(f"{message_numb}:STATE:SET\n".encode('ascii'))

        if delta > 12 and not play:
            play = True
            s.sendall(f"{message_numb}:STATE:PLAY\n".encode('ascii'))

        if delta > 15 and play and not penalized:
            penalized = True
            s.sendall(f"{message_numb}:PENALTY:27:1:BALL_MANIPULATION\n".encode('ascii'))

        if delta > 17 and play and not scored:
            scored = True
            s.sendall(f"{message_numb}:SCORE:27\n".encode('ascii'))

        data = s.recv(1024)
        if data:
            print(data)

        message_numb += 1
        time.sleep(0.2)

