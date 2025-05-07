# Deployment Guide for DemoApp on Render

## Prerequisites
1. GitHub account with your code repository
2. Render account (https://render.com)
3. All services built and tested locally

## Pre-deployment Steps

1. Build all services:
   ```bash
   cd services/config-server && ./mvnw clean package -DskipTests
   cd ../discovery && ./mvnw clean package -DskipTests
   cd ../api && ./mvnw clean package -DskipTests
   cd ../menu && ./mvnw clean package -DskipTests
   cd ../mess && ./mvnw clean package -DskipTests
   cd ../subscription && ./mvnw clean package -DskipTests
   cd ../authentication && ./mvnw clean package -DskipTests
   ```

2. Ensure all Dockerfiles are present in respective service directories
3. Verify render.yaml is in the root directory

## Deployment Steps

1. Push your code to GitHub:
   ```bash
   git add .
   git commit -m "Prepare for Render deployment"
   git push origin main
   ```

2. On Render Dashboard:
   - Click "New +"
   - Select "Blueprint"
   - Connect your GitHub repository
   - Select the repository with your DemoApp

3. Environment Variables:
   Set the following in Render dashboard:
   - JWT_SECRET (generate a secure random string)
   - SPRING_PROFILES_ACTIVE=prod

4. Deployment Order:
   Services will deploy in this order:
   1. Config Server
   2. Discovery Service
   3. Databases
   4. Other Services

## Post-deployment Steps

1. Verify Services:
   - Check each service's health endpoint
   - Verify service registration in Eureka
   - Test API endpoints through gateway

2. Monitor:
   - Check logs for each service
   - Monitor database connections
   - Verify service discovery

## Troubleshooting

1. If services fail to register:
   - Check DISCOVERY_URL environment variable
   - Verify network connectivity
   - Check service logs

2. Database connection issues:
   - Verify database credentials
   - Check database status in Render
   - Ensure proper connection strings

3. Memory issues:
   - Monitor service memory usage
   - Adjust JVM parameters if needed

## Important Notes

1. Free Tier Limitations:
   - Services sleep after 15 minutes of inactivity
   - Limited database rows
   - Shared CPU resources

2. Security:
   - JWT_SECRET must be secure
   - Database access is limited to Render network
   - API endpoints are HTTPS by default

3. Scaling:
   - Consider upgrading to paid plans for production
   - Monitor resource usage
   - Set up alerts for service health

## Support

For issues:
1. Check Render documentation
2. Review service logs
3. Contact Render support if needed 