# ZIO rite of passage

This a full stack Scala 3 project using ZIO.

This project is application of [ZIO rite of passage](https://courses.rockthejvm.com/p/zio-rite-of-passage) course.

I encourage you to check it out if you want to learn ZIO in depth and build real-world applications with it.

More generaly, if you want to learn any JVM related technology, I highly recommend to follow [@rockthejvm](https://x.com/rockthejvm), checks courses [courses](https://courses.rockthejvm.com), blogs [blogs](https://blog.rockthejvm.com) they are very well structured, explained and go straight to the point.


## What is inside ?


* Fullstack Scala 3 application
  * isomorphic code sharing between backend and frontend
  * ZIO powered backend and frontend
* A ZIO backend
  * Tapir
  * ZIO quill
* A laminar frontend
  * ZIO
  * Tapir client

## Usage

I try as hobby to automate as much as possible the setup of the project.


### Prequisites

Sbt project using docker to run the database.

* Install [Docker](https://www.docker.com/get-started/)
* Install [NodeJS](https://nodejs.org/en/download/)
* Install [sbt](https://www.scala-sbt.org/download.html)

### Development

THis project is intended to be run with vscode / metals

```bash
git clone git@github.com:cheleb/zio-rite-of-passage.git
code .
```

This will open the project in vscode and setup everything.
With finished the setup you can let metals import the sbt project.


And then enjoy the development experience with hot reload for both backend and frontend.

### Standalone usage

To run as a standalone application you can use the following commands.

To start the database:

```bash
docker-compose up -d
```

To start the backend:

```bash
./scripts/fullstackRun.sh
```

Application will be available at `http://localhost:4041/`
