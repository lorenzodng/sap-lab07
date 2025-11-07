#### Software Architecture and Platforms - a.y. 2025-2026

## Example of interactive session with `distributed_ttt`

v1.0.0-20251031

Steps:

- Run the services
  - Open a shell in `lab-activity-06` directory
  - run the `AccountService` (port 9000):   

    	mvn compile exec:java -Dexec.mainClass="distributed_ttt.account_service.infrastructure.AccountServiceMain"`

  - run the `LobbyService` (port 9001):   

    	mvn compile exec:java -Dexec.mainClass="distributed_ttt.lobby_service.infrastructure.LobbyServiceMain"`
  
  - run the `GameService` (port 9002):   

    	mvn compile exec:java -Dexec.mainClass="distributed_ttt.game_service.infrastructure.GameServiceMain"`


- Register the first user by interacting with the `AccountService`
  - request: `POST http://localhost:9000/api/v1/accounts` with payload: 

		{ 	
			"userName": "user1", 
			"password": "123secret" 
		}

  - response: `200` with payload: 

		{ 
			"loginLink": "/api/v1/accounts/user1/login", 
			"accountLink": "/api/v1/accounts/user1" 
		}

- Get first user info by interacting with the `AccountService`
  - request: `GET http://localhost:9000/api/v1/accounts/user1` 


  - response: `200` with payload: 

		{
    		"accountInfo": {
        		"userName": "user1",
        		"password": "123secret",
        		"whenCreated": 1761854476658
    		}
		}

- Register the second user by interacting with the `AccountService`:
  - request: `POST http://localhost:9000/api/v1/accounts`with payload: 

		{ 
			"userName": "user2", 
			"password":	"456secret" 
		}

  - response: `200` with payload: 
		
		{ 
			"loginLink": "/api/v1/accounts/user2/login", 
			"accountLink": "/api/v1/accounts/user2" 
		}


- First user login by interacting with the `LobbyService`
  - request: `POST http://localhost:9001/api/v1/lobby/login` with payload:

		{ 
			"userName": "user1", 
			"password":	"123secret" 
		}

  - response: `200` with payload: 

		{
    		"sessionId": "user-session-1",
    		"createGameLink": "/api/v1/lobby/user-sessions/user-session-1/create-game",
    		"joinGameLink": "/api/v1/lobby/user-sessions/user-session-1/join-game",
    		"sessionLink": "/api/v1/lobby/user-sessions/user-session-1"
		}

- Second user login  by interacting with the `LobbyService`
  - request: `POST http://localhost:9001/api/v1/lobby/login` with payload:

		{ 
			"userName": "user2", 
			"password":	"456secret" 
		}

  - response: `200` with payload: 

		{
    		"sessionId": "user-session-2",
			"createGameLink": "/api/v1/user-sessions/user-session-2/create-game",
    		"joinGameLink": "/api/v1/user-sessions/user-session-2/join-game",
    		"sessionLink": "/api/v1/user-sessions/user-session-2"
		}

- Game creation by interacting with the `LobbyService`
	- request: `POST http://localhost:9001/api/v1/lobby/user-sessions/user-session-1/create-game` with payload:
		
		
			{ 
				"gameId": "super-game"
			}


    - response: `200`with payload:

			{
    			"result": "ok",
    			"gameLink": "http://localhost:9002/api/v1/games/super-game",
    			"joinGameLink": "/api/v1/lobby/user-sessions/user-session-1/join-game"
			}

- Get game info:
  - request: `GET http://localhost:9002/api/v1/games/super-game`

  - response: `200` with payload: 

		{
    		"result": "ok",
    		"gameInfo": {
        		"gameId": "super-game",
        		"gameState": "waiting-for-players"
			}
    	}
	

- First user joining the game by interacting with the `LobbyService`
  - request: `POST http://localhost:9001/api/v1/lobby/user-sessions/user-session-1/join-game` with payload:

		{
    		"gameId": "super-game",
    		"symbol": "X" 
		}

  - response: `200` with payload: 

		{
    		"playerSessionId": "player-session-1",
    		"moveLink": "/api/v1/games/super-game/player-session-1/move",
    		"gameLink": "/api/v1/games/super-game",
    		"playerSessionLink": "/api/v1/games/super-game/player-X",
			"eventChannelLink": "/api/v1/events"
		}

- First user connecting an event channel by interacting with the `GameService`
  - web-socket request: `ws://localhost:9002/api/v1/events`
  - After that web-socket has connected, a message over the channel is sent with payload

		{
			"playerSessionId": "player-session-1"
		}
	 


- Second user joining the game
  - request: `POST  http://localhost:9001/api/v1/lobby/user-sessions/user-session-2/join-game` with payload:

		{ 
			"gameId": "super-game",
			"symbol": "O" 
		}

  - response: `200` with payload: 

		{
    		"playerSessionId": "player-session-2",
    		"moveLink": "/api/v1/games/super-game/player-session-2/move",
    		"gameLink": "/api/v1/games/super-game",
    		"playerSessionLink": "/api/v1/games/super-game/player-O",
			"eventChannelLink": "/api/v1/events"
		}

- Second user connecting an event channel:
  - web-socket request: `ws://localhost:9002/api/v1/events`
  - After that web-socket has connected, a message over the channel is sent with payload

		{
			"playerSessionId": "player-session-2"
		}


- Get game info again:
  - request: `GET http://localhost:9002/api/v1/games/super-game`

  - response: `200` with payload: 

		{
    		"gameInfo": {
        		"gameId": "super-game",
        		"gameState": "started",
        		"boardState": [ "-","-","-","-","-","-","-","-","-"],
        		"turn": "X"
    		}
		}

- First user making a move
  - request: `POST http://localhost:9002/api/v1/games/super-game/player-session-1/move` with payload:

		{
    		"x": 0,
    		"y": 0
		}

  - response: `200` with payload: 

		{
    		"result": "accepted",
    		"moveLink": "/api/v1/games/super-game/player-session-1/move",
    		"gameLink": "/api/v1/games/super-game"
		}

- Second user making a move
  - request: `POST http://localhost:9002/api/v1/games/super-game/player-session-2/move` with payload:

		{
    		"x": 1,
    		"y": 1
		}

  - response: `200` with payload: 

		{
    		"result": "accepted",
    		"moveLink": "/api/v1/games/super-game/player-session-2/move",
    		"gameLink": "/api/v1/games/super-game"
		}

- Get game info again:
  - request: `GET http://localhost:9002/api/v1/games/super-game`

  - response: `200` with payload: 

		{
    		"gameInfo": {
        		"gameId": "super-game",
        		"gameState": "started",
        		"boardState": [ "X","-","-","-","O","-","-","-","-"],
        		"turn": "X"
    		}
		}




  

