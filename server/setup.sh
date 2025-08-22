#/usr/bin/env bash

# Setup on Hetzner Cloud

server_ip="your_server_ip"

sudo apt update
sudo apt install -y docker.io
sudo systemctl start docker
sudo systemctl enable docker

sudo add-apt-repository -y ppa:longsleep/golang-backports
sudo apt update
sudo apt install -y golang-go

mkdir server

sudo usermod -aG docker $USER
logout
