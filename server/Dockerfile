FROM golang:1.24
WORKDIR /app
COPY . .
RUN go build -o server
EXPOSE 80 3000
CMD ["./server"]
