
# Online Judge

> WIP

## API Reference
[Wiki](https://github.com/kastnerorz/shu-online-judge/wiki)

## Depoly

### First Deploy
#### Frontend Deploy

1. Install node > 9.

2. Install yarn.

3. Run

```shell
cd frontend
yarn install
yarn serve
```
#### Backend Deploy

##### Using Docker (Recommand)

1. Install [Docker](https://docs.docker.com/install/linux/docker-ce/ubuntu/).

2. Clone the project:

```shell
git clone git@github.com:kastnerorz/shu-online-judge.git
```

3. Build the docker image:

```shell
mvn package -Dmaven.test.skip=true dockerfile:build
```

4. Write a new `docker-compose.yml` like this:

```yaml
version: '2'
services:
  shu-online-judge:
    image: kastnerorz/oj:latest
    container_name: oj
    environment:
      - SPRING_DATASOURCE_URL=
      - SPRING_DATASOURCE_USERNAME=
      - SPRING_DATASOURCE_PASSWORD=
      - SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE=30Mb
      - SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE=100Mb
      - SERVER_PORT=8081
      - UPLOAD_PATH=/var/lib/upload
    ports:
      - 8081:8081
    volumes:
      - ~/upload/shu-online-judge:/var/lib/upload
```

5. Start Server:

```shell
docker-compose up -d
```

6. Server will serve at `http://localhost:8081`.

#### Frontend Deploy

##### Using Docker (Recommand)

1. Goto frontend folder:

```shell
cd frontend
```

2. Write a `Dockerfile` like this:

```dockerfile
FROM node:9.11.1-alpine

# install simple http server for serving static content
RUN yarn global add http-server

# make the 'app' folder the current working directory
WORKDIR /app

# copy both 'package.json' and 'package-lock.json' (if available)
COPY package*.json ./
COPY yarn.lock ./

# install project dependencies
RUN yarn

# copy project files and folders to the current working directory (i.e. 'app' folder)
COPY . .

# build app for production with minification
RUN yarn run build

EXPOSE 8085

CMD [ "http-server","-p","8085","dist" ]
```

3. Build the docker image:

```shell
docker build -t kastnerorz/oj-frontend .
```

4. Run the container:

```shell
docker run --name oj-frontend -p 8085:8085 -d kastnerorz/oj-frontend
```

5. Frontend will serve at `http://localhost:8085`.
