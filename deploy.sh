#!/bin/bash
# Path: p2pfilesharing/deploy.sh

set -e

# Configuration
DOMAIN="your-domain.com"
EMAIL="your-email@example.com"
SERVER_USER="ubuntu"
SERVER_IP="your-server-ip"

echo "🚀 Starting P2P File Share deployment..."

# Update frontend API URL for production
echo "📝 Updating frontend configuration..."
sed -i.bak "s|http://localhost:8080|https://$DOMAIN|g" frontend/src/App.js

# Build and push Docker images (optional - for Docker Hub)
echo "🐳 Building Docker images..."
docker build -t p2p-backend ./backend
docker build -t p2p-frontend ./frontend

# Create deployment directory structure
ssh $SERVER_USER@$SERVER_IP "mkdir -p ~/p2p-app/{nginx,ssl}"

# Copy files to server
echo "📁 Copying files to server..."
scp -r . $SERVER_USER@$SERVER_IP:~/p2p-app/

# Install dependencies on server
echo "⚙️ Installing dependencies on server..."
ssh $SERVER_USER@$SERVER_IP << 'EOF'
# Update system
sudo apt update && sudo apt upgrade -y

# Install Docker
if ! command -v docker &> /dev/null; then
    curl -fsSL https://get.docker.com -o get-docker.sh
    sudo sh get-docker.sh
    sudo usermod -aG docker $USER
fi

# Install Docker Compose
if ! command -v docker-compose &> /dev/null; then
    sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
fi

# Install Certbot for SSL
sudo apt install -y certbot

cd ~/p2p-app
EOF

# Setup SSL certificate
echo "🔒 Setting up SSL certificate..."
ssh $SERVER_USER@$SERVER_IP << EOF
# Get SSL certificate
sudo certbot certonly --standalone -d $DOMAIN -d www.$DOMAIN --email $EMAIL --agree-tos --no-eff-email

# Copy certificates to project directory
sudo cp /etc/letsencrypt/live/$DOMAIN/fullchain.pem ~/p2p-app/nginx/ssl/
sudo cp /etc/letsencrypt/live/$DOMAIN/privkey.pem ~/p2p-app/nginx/ssl/
sudo chown -R $USER:$USER ~/p2p-app/nginx/ssl/
EOF

# Update nginx configuration with actual domain
ssh $SERVER_USER@$SERVER_IP "sed -i 's/your-domain.com/$DOMAIN/g' ~/p2p-app/nginx/nginx.conf"

# Deploy application
echo "🚀 Deploying application..."
ssh $SERVER_USER@$SERVER_IP << 'EOF'
cd ~/p2p-app

# Stop existing containers
docker-compose down

# Build and start new containers
docker-compose up --build -d

# Show status
docker-compose ps
EOF

echo "✅ Deployment completed!"
echo "🌐 Your application should be available at: https://$DOMAIN"
echo "📊 Check status with: ssh $SERVER_USER@$SERVER_IP 'cd ~/p2p-app && docker-compose ps'"