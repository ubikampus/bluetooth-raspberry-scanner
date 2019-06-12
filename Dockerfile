FROM node:8.16.0-jessie

WORKDIR /app
COPY package.json .
COPY package-lock.json .

RUN npm install node-gyp
RUN npm i
COPY /src ./src
CMD ["node", "src/app.js"]