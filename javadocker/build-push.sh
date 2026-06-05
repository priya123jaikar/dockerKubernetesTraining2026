#!/bin/bash

# Build and push Docker images for Blue-Green Deployment

DOCKER_REGISTRY="priyasocgen"
APP_NAME="java-web"

echo "================================"
echo "Building Blue-Green Docker Images"
echo "================================"

# Build BLUE image
echo ""
echo "Building BLUE image (v1.0)..."
docker build -f Dockerfile.blue -t $DOCKER_REGISTRY/$APP_NAME-blue:v1.0 .
docker tag $DOCKER_REGISTRY/$APP_NAME-blue:v1.0 $DOCKER_REGISTRY/$APP_NAME-blue:latest

# Build GREEN image
echo ""
echo "Building GREEN image (v2.0)..."
docker build -f Dockerfile.green -t $DOCKER_REGISTRY/$APP_NAME-green:v2.0 .
docker tag $DOCKER_REGISTRY/$APP_NAME-green:v2.0 $DOCKER_REGISTRY/$APP_NAME-green:latest

echo ""
echo "Images built successfully!"
echo ""
echo "Image summary:"
echo "  BLUE:  $DOCKER_REGISTRY/$APP_NAME-blue:v1.0"
echo "  GREEN: $DOCKER_REGISTRY/$APP_NAME-green:v2.0"
echo ""

# Push to registry (optional)
read -p "Push images to Docker registry? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "Pushing BLUE image..."
    docker push $DOCKER_REGISTRY/$APP_NAME-blue:v1.0
    docker push $DOCKER_REGISTRY/$APP_NAME-blue:latest
    
    echo "Pushing GREEN image..."
    docker push $DOCKER_REGISTRY/$APP_NAME-green:v2.0
    docker push $DOCKER_REGISTRY/$APP_NAME-green:latest
    
    echo ""
    echo "Images pushed successfully!"
fi

echo ""
echo "================================"
echo "Next steps:"
echo "================================"
echo "1. Run locally with Docker Compose:"
echo "   docker-compose -f docker-compose-blue-green.yml up"
echo ""
echo "2. Deploy to Kubernetes:"
echo "   kubectl apply -f manifest-blue-green.yaml"
echo ""
echo "3. Access applications:"
echo "   BLUE:  http://localhost:8080"
echo "   GREEN: http://localhost:8081"