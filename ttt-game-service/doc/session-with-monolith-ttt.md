#### Software Architecture and Platforms - a.y. 2025-2026

## Example of interactive session with `monolith_ttt_game_server`

v1.0.0-20251031

Steps:

- Run the game server 
  - Open a shell in `lab-activity-06` directory
  - Run the command:   

    	mvn compile exec:java -Dexec.mainClass="monolith_ttt_game_server.infrastructure.TTTGameServerMain"`
  
- Register the first user
  - request: `POST http://localhost:8080/api/v1/accounts` with payload: 

		{ 	
			"userName": "user1", 
			"password": "123secret" 
		}

  - response: `200` with payload: 

		{ 
			"loginLink": "/api/v1/accounts/user1/login", 
			"accountLink": "/api/v1/accounts/user1" 
		}

- Register the second user
  - request: `POST http://localhost:8080/api/v1/accounts`with payload: 

		{ 
			"userName": "user2", 
			"password":	"456secret" 
		}

  - response: `200` with payload: 
		
		{ 
			"loginLink": "/api/v1/accounts/user2/login", 
			"accountLink": "/api/v1/accounts/user2" 
		}


- First user login
  - request: `POST http://localhost:8080/api/v1/accounts/user1/login` with payload:

		{ 
			"password":	"123secret" 
		}

  - response: `200` with payload: 

		{
    		"sessionId": "user-session-1",
			"createGameLink": "/api/v1/user-sessions/user-session-1/create-game",
    		"joinGameLink": "/api/v1/user-sessions/user-session-1/join-game",
    		"sessionLink": "/api/v1/user-sessions/user-session-1"
		}

- Second user login
  - request: `POST http://localhost:8080/api/v1/accounts/user2/login` with payload:

		{ 
			"password":	"456secret" 
		}

  - response: `200` with payload: 

		{
    		"sessionId": "user-session-2",
			"createGameLink": "/api/v1/user-sessions/user-session-2/create-game",
    		"joinGameLink": "/api/v1/user-sessions/user-session-2/join-game",
    		"sessionLink": "/api/v1/user-sessions/user-session-2"
		}

- Game creation
	- request: `POST http://localhost:8080/api/v1/user-sessions/user-session-1/create-game` with payload:

		{ 
			"gameId": "super-game" 
		}

- First user joining the game
  - request: `POST http://localhost:8080/api/v1/user-sessions/user-session-1/join-game` with payload:

		{ 
			"sessionId": "user-session-1",
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

- First user connecting an event channel:
  - web-socket request: `ws://localhost:8080/api/v1/events`
  - After that web-socket has connected, a message over the channel is sent with payload

		{
			"playerSessionId": "player-session-1"
		}
	 


- Second user joining the game
  - request: `POST http://localhost:8080/api/v1/user-sessions/user-session-2/join-game` with payload:

		{ 
			"sessionId": "user-session-2",
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
  - web-socket request: `ws://localhost:8080/api/v1/events`
  - After that web-socket has connected, a message over the channel is sent with payload

		{
			"playerSessionId": "player-session-2"
		}
	 


- First user making a move
  - request: `POST http://localhost:8080/api/v1/games/super-game/player-session-1/move` with payload:

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
  - request: `POST http://localhost:8080/api/v1/games/super-game/player-session-2/move` with payload:

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





  

