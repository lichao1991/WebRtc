/**
 * Module dependencies.
 */
var express = require('express')
,	path = require('path')
,	streams = require('./app/streams.js')();

//https setting start
var fs = require('fs');
var http = require('http');
var https = require('https');
var privateKey  = fs.readFileSync('./private.pem', 'utf8');
var certificate = fs.readFileSync('./file.crt', 'utf8');
var credentials = {key: privateKey, cert: certificate};


var PORT = 18080;
var SSLPORT = 18081;
//end

var favicon = require('serve-favicon')
,	logger = require('morgan')
,	methodOverride = require('method-override')
,	bodyParser = require('body-parser')
,	errorHandler = require('errorhandler');

var app = express();

// all environments
app.set('port', SSLPORT || PORT);
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'ejs');
app.use(favicon(__dirname + '/public/images/favicon.ico'));
app.use(logger('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));
app.use(methodOverride());
app.use(express.static(path.join(__dirname, 'public')));

// var httpServer = http.createServer(app);
var httpsServer = https.createServer(credentials, app);

// development only
if ('development' == app.get('env')) {
  app.use(errorHandler());
}

// routing
require('./app/routes.js')(app, streams);

var server = app.listen(PORT, function(){
  console.log('Express server listening on port ' + PORT);
});

// httpServer.listen(PORT, function() {
//     console.log('HTTP Server is running on: http://localhost:%s', PORT);
// });

httpsServer.listen(SSLPORT, function() {
    console.log('HTTPS Server is running on: https://localhost:%s', SSLPORT);
});

var io = require('socket.io').listen(httpsServer);
var ioHttp = require('socket.io').listen(server);



/**
 * Socket.io event handling
 */
require('./app/socketHandler.js')(io, streams);

require('./app/socketHandler.js')(ioHttp, streams);

