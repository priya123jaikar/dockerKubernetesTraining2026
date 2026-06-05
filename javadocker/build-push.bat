@echo off
REM Build and push Docker images for Blue-Green Deployment

set DOCKER_REGISTRY=priyasocgen
set APP_NAME=java-web

echo.
echo ================================
echo Building Blue-Green Docker Images
echo ================================
echo.

REM Build BLUE image
echo Building BLUE image (v1.0)...
docker build -f Dockerfile.blue -t %DOCKER_REGISTRY%/%APP_NAME%-blue:v1.0 .
docker tag %DOCKER_REGISTRY%/%APP_NAME%-blue:v1.0 %DOCKER_REGISTRY%/%APP_NAME%-blue:latest

echo.

REM Build GREEN image
echo Building GREEN image (v2.0)...
docker build -f Dockerfile.green -t %DOCKER_REGISTRY%/%APP_NAME%-green:v2.0 .
docker tag %DOCKER_REGISTRY%/%APP_NAME%-green:v2.0 %DOCKER_REGISTRY%/%APP_NAME%-green:latest

echo.
echo Images built successfully!
echo.
echo Image summary:
echo   BLUE:  %DOCKER_REGISTRY%/%APP_NAME%-blue:v1.0
echo   GREEN: %DOCKER_REGISTRY%/%APP_NAME%-green:v2.0
echo.
echo.
echo ================================
echo Next steps:
echo ================================
echo 1. Run locally with Docker Compose:
echo    docker-compose -f docker-compose-blue-green.yml up
echo.
echo 2. Push to registry (edit build-push.bat with your registry):
echo    docker push %DOCKER_REGISTRY%/%APP_NAME%-blue:v1.0
echo    docker push %DOCKER_REGISTRY%/%APP_NAME%-green:v2.0
echo.
echo 3. Deploy to Kubernetes:
echo    kubectl apply -f manifest-blue-green.yaml
echo.
echo 4. Access applications:
echo    BLUE:  http://localhost:8080
echo    GREEN: http://localhost:8081
echo.
pause