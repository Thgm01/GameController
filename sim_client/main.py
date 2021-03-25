#!/usr/bin/env python3

import socket
import time

HOST = '127.0.0.1'  # The server's hostname or IP address
PORT = 8750        # The port used by the server



with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
    s.connect((HOST, PORT))

    start = time.time()
    ready, set, play = False, False, False

    PACKAGE_NR:INSTRUCTION:TEAM:PLAYER:PENALTY
    1:CLOCK:13
    2:STATE:READY
    3:PENALTY:0:2:killed_robot
    4:SCORE:0


    while True:
        delta = time.time() - start

        clock_data = f'CLOCK:{int(delta * 3 * 1000)}\n'
        print(clock_data)

        s.sendall(clock_data.encode('ascii'))
        s.recv(1024)

        if delta > 3 and not ready:
            ready = True
            s.sendall("STATE:READY\n".encode('ascii'))
            s.recv(1024)

        if delta > 7 and not set:
            set = True
            s.sendall("STATE:SET\n".encode('ascii'))
            s.recv(1024)

        if delta > 12 and not play:
            play = True
            s.sendall("STATE:PLAY\n".encode('ascii'))
            s.recv(1024)

        time.sleep(0.2)

