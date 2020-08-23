---
--- Generated by EmmyLua(https://github.com/EmmyLua)
--- Created by Yunxi.
--- DateTime: 2019/12/25 15:40
--- 抢购取消脚本（用户主动取消，被动取消）
---

local useripKey = KEYS[1];
local useridKey = KEYS[2];
local userip = ARGV[1];
local userid = ARGV[2];

-- 判断redis中是否存有此IP
local ipExist = redis.call('HEXISTS', useripKey, ip);

if ipExist == 1 then
    redis.call('HDEL', userip, ip);
end

-- 判断redis中是否存有此用户Id
local idExist = redis.call('HEXISTS', useridKey, userid);
if idExist == 1 then
    redis.call('HDEL', useridKey, userid);
end

return -100;