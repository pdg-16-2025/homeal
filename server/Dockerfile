FROM golang:1.24

# Install Python3 and pip
RUN apt-get update && apt-get install -y \
    sqlite3 \
    python3 \
    python3-pip \
    python3-dev \
    && rm -rf /var/lib/apt/lists/*

# Install Python dependencies
RUN pip3 install --break-system-packages numpy pandas

WORKDIR /app
COPY . .

# Build Go application
RUN go build -o server

EXPOSE 80 3000
CMD ["./server"]

# docker run -p 80:80 -p 3000:3000 <image_id>