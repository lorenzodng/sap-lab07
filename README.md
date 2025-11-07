#### Software Architecture and Platforms - a.y. 2025-2026

## Lab Activity #07-20251107  

v1.0.0-20251105

- **Deploying microservices** with Docker - [Lab Notes](https://docs.google.com/document/d/1f31xacpq7LNfKfoR77aNZnamDTNwVRGUpVf8t8hA3ew/edit?usp=sharing)
  - Example in Lab Notes: TTT Game Server case study
    - three repos, each for a different microservice
      - `ttt-account-service`
      - `ttt-lobby-service`
      - `ttt-game-service`
- **API Gateway** microservices pattern - [Lab Notes](https://docs.google.com/document/d/1SO1q7uRvtsIMaA_7niKKvATEIDmD1Z9ddXnaSkv5FUw/edit?usp=sharing)
  - Example in the TTT Game Server case study
    - `ttt-api-gateway` - a separate repo/microservice implementing a simple API Gateway
      - using an hexagonal architecture, with proxies to interact with services 
        - the business logic is based on the model of the services, from the API Gateway perspective
      - an asynchronous communication model is adopted, based on `Vert.x` event loop control architecture
 




 





  

