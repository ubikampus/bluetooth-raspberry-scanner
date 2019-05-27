FROM node:8.16.0-jessie

WORKDIR /app
COPY package.json .
COPY package-lock.json .
RUN gcc -v
RUN python --version
RUN make -v

RUN npm install node-gyp
RUN npm i
COPY app.js .
CMD ["node", "app.js"]