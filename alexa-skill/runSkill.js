/**
 * This allow test skill on PC
 */

var skill = require('./AlexaSkill.js');
var fs = require('fs');

//var request = JSON.parse(fs.readFileSync('request-discovery.json', 'utf8'));
var request = JSON.parse(fs.readFileSync('request-control.json', 'utf8'));


skill.handler(request, {}, function(val){
    console.log("CALLBACK : ", val);
});