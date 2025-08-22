#/usr/bin/env bash

# Run this script on the server to set up everything

sudo apt update
sudo apt install -y docker.io
sudo systemctl start docker
sudo systemctl enable docker

sudo usermod -aG docker $USER
logout

docker pull vasilba/homeal:latest
docker run -d -p 8080:8080 vasilba/homeal:latest
