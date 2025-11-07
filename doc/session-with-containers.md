#### Software Architecture and Platforms - a.y. 2025-2026

## Example of interactive session with dockerised TTT Game Server

v1.0.0-20251107

Context: we have three microservices (`ttt-account-service`, `ttt-lobby-service`, `ttt-game-service`) to be deployed in dockers containers. Each microservice has its own repo.

**Steps:**

- Build the docker images for each microservice, including the API Gateway  (from each corresponding repo directory):
  
	`docker build -t ttt-account-service -f Dockerfile .`  
	`docker build -t ttt-game-service -f Dockerfile .`  
	`docker build -t ttt-lobby-service -f Dockerfile .`  
	`docker build -t ttt-api-gateway -f Dockerfile .`

- Run the containers using `docker compose`, setting up a logical network among them. The `docker-compose.yaml` is located in the root of this lab activity repo. Open a terminal on that directory and type:

	`docker compose up`  
  
- Start interacting with the services. Two possibilities:
  - either interacting with each individual service,  as described in previous lab activity doc [session-with-distributed-ttt](https://github.com/sap-2025-2026/lab-activity-06/blob/main/doc/session-with-distributed-ttt.md)
  - or **through the API Gateway**, which is listening at port 8080:
    - to create an account: `POST http://localhost:8080/api/v1/accounts` with payload `{ "userName": "user1", "password": "123secret" }`
    - to get account info: `GET http://localhost:8080/api/v1/accounts/user-1`
    - to login: `POST http://localhost:8080/api/v1/lobby/login`  with payload `{ "userName": "user1", "password": "123secret" }`
    - to create a game:  `POST http://localhost:8080/api/v1/lobby/user-sessions/user-session-1/create-game`  with payload `{ "gameId": "super-game" }`
    - to join a game: `POST http://localhost:8080/api/v1/lobby/user-sessions/user-session-1/join-game`  with payload `{ "gameId": "super-game", "symbol": "X" }`
    - to get game info: `GET http://localhost:8080/api/v1/games/super-game`  
    - to make a move: `POST http://localhost:8080/api/v1/games/super-game/player-session-1/move` with payload `{ "x": 0, "y": 0}`

		






  

