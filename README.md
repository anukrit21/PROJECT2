<<<<<<< HEAD
# DemoApp with Recommendations

This repository contains an enhanced version of DemoApp with mess recommendations functionality and campus module integration.

## Quick Start Guide

### Running the Services

We've provided several scripts to make running the services easier:

1. **Run everything (Mess Service + Campus Module + Mock API Server)**:
   ```
   .\run-mess-and-campus.bat
   ```

2. **Run only the Mess Service with Recommendations**:
   ```
   .\run-mess-service.bat
   ```

3. **Run only the Campus Module**:
   ```
   .\run-campus-module.bat
   ```

4. **Run only the Mock API Server**:
   ```
   .\run-mock-server.bat
   ```

### Available Documentation

- [Campus Module Guide](CAMPUS_MODULE_GUIDE.md) - Details about the Campus Module integration
- [Recommendations API Documentation](docs/RECOMMENDATIONS_API.md) - API documentation for recommendations
- [Frontend Integration Guide](docs/FRONTEND_INTEGRATION_GUIDE.md) - Guide for frontend developers

## Project Overview
DemoApp is a microservices-based food delivery application that connects users with local food services. The application is built using Spring Boot for the backend and is designed to be integrated with any frontend framework.

## Architecture
The application follows a microservices architecture with the following key components:
- Eureka Server: Service discovery
- API Gateway: Entry point for all frontend requests
- Authentication Service: Handles user authentication and authorization
- User Service: Manages user profiles and preferences
- Menu Module: Handles menu items and categories
- Order Service: Processes food orders
- Payment Service: Manages payment transactions
- Delivery Service: Handles delivery tracking
- Mess Service: Manages mess-related operations
- Subscription Service: Handles subscription plans

## For Frontend Developers

### Getting Started
1. Clone the repository
```bash
git clone https://gitlab.com/beleharshwardhan/demoApp.git
cd demoApp
```

2. Required Environment Variables
```env
API_GATEWAY_URL=http://localhost:8080
JWT_SECRET=your_jwt_secret
ENVIRONMENT=development
```

3. API Documentation
- Comprehensive API documentation is available in [API_DOCUMENTATION.md](./API_DOCUMENTATION.md)
- All API requests should go through the API Gateway
- Authentication is required for most endpoints

### Development Guidelines
1. Authentication
   - Use JWT tokens for authentication
   - Include token in Authorization header
   - Refresh tokens when needed

2. API Interaction
   - All requests must go through the API Gateway
   - Use appropriate error handling
   - Follow rate limiting guidelines

3. Real-time Features
   - WebSocket connection available for real-time updates
   - Subscribe to relevant topics for live updates

### Local Development Setup
1. Ensure backend services are running:
   ```bash
   docker-compose up
   ```
2. Configure your frontend application to use the local API Gateway
3. Use the test endpoints for development

### Testing
- Test credentials are available in the API documentation
- Use test endpoints with `/test` prefix
- Mock API responses are available for offline development

## Deployment
Refer to [DEPLOYMENT.md](./DEPLOYMENT.md) for detailed deployment instructions.

## Support
- Technical issues: Create an issue in GitLab
- API questions: Contact the backend team
- Documentation updates: Submit a merge request

## Contributing
1. Create a feature branch
2. Make your changes
3. Submit a merge request
4. Follow the code review process

## License
[Add License Information]
=======
# DemoApp


>>>>>>> b36a35a71a5c2189e8840e06afa97478a44e9109

## Getting started

To make it easy for you to get started with GitLab, here's a list of recommended next steps.

Already a pro? Just edit this README.md and make it your own. Want to make it easy? [Use the template at the bottom](#editing-this-readme)!

## Add your files

- [ ] [Create](https://docs.gitlab.com/ee/user/project/repository/web_editor.html#create-a-file) or [upload](https://docs.gitlab.com/ee/user/project/repository/web_editor.html#upload-a-file) files
- [ ] [Add files using the command line](https://docs.gitlab.com/ee/gitlab-basics/add-file.html#add-a-file-using-the-command-line) or push an existing Git repository with the following command:

```
cd existing_repo
git remote add origin https://gitlab.com/beleharshwardhan/demoApp.git
git branch -M main
git push -uf origin main
```

## Integrate with your tools

- [ ] [Set up project integrations](https://gitlab.com/beleharshwardhan/demoApp/-/settings/integrations)

## Collaborate with your team

- [ ] [Invite team members and collaborators](https://docs.gitlab.com/ee/user/project/members/)
- [ ] [Create a new merge request](https://docs.gitlab.com/ee/user/project/merge_requests/creating_merge_requests.html)
- [ ] [Automatically close issues from merge requests](https://docs.gitlab.com/ee/user/project/issues/managing_issues.html#closing-issues-automatically)
- [ ] [Enable merge request approvals](https://docs.gitlab.com/ee/user/project/merge_requests/approvals/)
- [ ] [Set auto-merge](https://docs.gitlab.com/ee/user/project/merge_requests/merge_when_pipeline_succeeds.html)

## Test and Deploy

Use the built-in continuous integration in GitLab.

- [ ] [Get started with GitLab CI/CD](https://docs.gitlab.com/ee/ci/quick_start/)
- [ ] [Analyze your code for known vulnerabilities with Static Application Security Testing (SAST)](https://docs.gitlab.com/ee/user/application_security/sast/)
- [ ] [Deploy to Kubernetes, Amazon EC2, or Amazon ECS using Auto Deploy](https://docs.gitlab.com/ee/topics/autodevops/requirements.html)
- [ ] [Use pull-based deployments for improved Kubernetes management](https://docs.gitlab.com/ee/user/clusters/agent/)
- [ ] [Set up protected environments](https://docs.gitlab.com/ee/ci/environments/protected_environments.html)

***

# Editing this README

When you're ready to make this README your own, just edit this file and use the handy template below (or feel free to structure it however you want - this is just a starting point!). Thanks to [makeareadme.com](https://www.makeareadme.com/) for this template.

## Suggestions for a good README

Every project is different, so consider which of these sections apply to yours. The sections used in the template are suggestions for most open source projects. Also keep in mind that while a README can be too long and detailed, too long is better than too short. If you think your README is too long, consider utilizing another form of documentation rather than cutting out information.

## Name
Choose a self-explaining name for your project.

## Description
Let people know what your project can do specifically. Provide context and add a link to any reference visitors might be unfamiliar with. A list of Features or a Background subsection can also be added here. If there are alternatives to your project, this is a good place to list differentiating factors.

## Badges
On some READMEs, you may see small images that convey metadata, such as whether or not all the tests are passing for the project. You can use Shields to add some to your README. Many services also have instructions for adding a badge.

## Visuals
Depending on what you are making, it can be a good idea to include screenshots or even a video (you'll frequently see GIFs rather than actual videos). Tools like ttygif can help, but check out Asciinema for a more sophisticated method.

## Installation
Within a particular ecosystem, there may be a common way of installing things, such as using Yarn, NuGet, or Homebrew. However, consider the possibility that whoever is reading your README is a novice and would like more guidance. Listing specific steps helps remove ambiguity and gets people to using your project as quickly as possible. If it only runs in a specific context like a particular programming language version or operating system or has dependencies that have to be installed manually, also add a Requirements subsection.

## Usage
Use examples liberally, and show the expected output if you can. It's helpful to have inline the smallest example of usage that you can demonstrate, while providing links to more sophisticated examples if they are too long to reasonably include in the README.

## Support
Tell people where they can go to for help. It can be any combination of an issue tracker, a chat room, an email address, etc.

## Roadmap
If you have ideas for releases in the future, it is a good idea to list them in the README.

## Contributing
State if you are open to contributions and what your requirements are for accepting them.

For people who want to make changes to your project, it's helpful to have some documentation on how to get started. Perhaps there is a script that they should run or some environment variables that they need to set. Make these steps explicit. These instructions could also be useful to your future self.

You can also document commands to lint the code or run tests. These steps help to ensure high code quality and reduce the likelihood that the changes inadvertently break something. Having instructions for running tests is especially helpful if it requires external setup, such as starting a Selenium server for testing in a browser.

## Authors and acknowledgment
Show your appreciation to those who have contributed to the project.

## License
For open source projects, say how it is licensed.

## Project status
If you have run out of energy or time for your project, put a note at the top of the README saying that development has slowed down or stopped completely. Someone may choose to fork your project or volunteer to step in as a maintainer or owner, allowing your project to keep going. You can also make an explicit request for maintainers.
<<<<<<< HEAD

## Quick Start Scripts

For the best experience running this application, use these scripts:

### Development/Demo Mode (Recommended)
- `fast-run.ps1` - Starts only PostgreSQL with all databases in Docker (fastest option)
- `fast-cleanup.ps1` - Stops the fast mode and cleans up resources
- `verify-database.ps1` - Tests the database connections and sample data

### Full Deployment Mode
- `run-with-fixes.ps1` - Starts all services with improved error handling
- `stop-services.ps1` - Stops all services properly
- `view-logs.ps1 [service-name]` - Views logs for all or specific services

### Troubleshooting
- `fix-docker.ps1` - Fixes common Docker issues and guides through resolution

See the `quick-start-guide.md` or `DOCKER-TROUBLESHOOTING.md` for more details.
=======
>>>>>>> b36a35a71a5c2189e8840e06afa97478a44e9109
#   P R O J E C T 2  
 