---
--- Generated by EmmyLua(https://github.com/EmmyLua)
--- Created by Yunxi.
--- DateTime: 2019/12/25 15:38
--- 商品抢购/限流/限购模块
---

-- 限制类型常量
local ipLimit = 1;
local idLimit = 2;
local ipAndIdLimit = 3;

-- 抢购活动类型
local activityType = KEYS[1];
-- 三个Key变量
local useripKey = KEYS[2];
local useridKey = KEYS[3];
local orderIdKey = KEYS[4];

--用户IP和用户Id
local userip = ARGV[1];
local userid = ARGV[2];

-- 限购类型：按用户或者按IP地址进行限购
local restrictType = tonumber(ARGV[3]);

-- 用户并发量的时间间隔,单位秒
local internal = tonumber(ARGV[4]);

-- 限购标识 1:非抢购商品，2:为抢购
local activityId = tonumber(ARGV[6]);
-- 抢购订单编号
local orderIdValue = ARGV[7];
-- 抢购数量
local purchaseNum = tonumber(ARGV[5]);
-- 抢购最小购买数
local minNum = tonumber(ARGV[8]);
-- 时间窗口内最大并发数
local maxNums = tonumber(ARGV[10]);

-- 时间间隔阈值
local threshold = "td";
-- 时间间隔中的请求次数
local requests = "rs";
-- 时间窗口
local timeWindow = "tw";

-- 抢购剩余数量
local balanceNum = "bn";
-- 活动状态(0：代表结束，1：正在进行)
local activityState="as";

-- userIP和userid默认失效时间
local expireTime = tonumber(ARGV[9]);
-- 抢购时间期限（秒为单位）
local ttlTime;

-- 抢购活动逻辑
if activityType == 2 then
    ttlTime = redis.call('TTL', activityId);
    -- 抢购活动不存在
    if -2 == ttlTime then
        return -907;
    end

    -- 活动结束
    if "0" == redis.call('HGET', activityId, activityState) then
        return -905;
    end

    -- maxNums不为0时限流，maxNums为0时关闭限流
    if maxNums ~= 0 then
        -- 第一次为获取抢购持续时间，后续为ttlTime
        local durationTime = redis.call('HGET', activityId, timeWindow);

        if durationTime ~= false then
            -- 时间间隔internal中的请求次数
            local counter = redis.call('HGET', activityId, requests);
            -- 时间间隔internal中的阀值
            local sn;
            if maxNums < 0 then
                sn = redis.call('HGET', activityId, threshold);
            else
                sn = maxNums;
            end ;

            if counter ~= false and sn ~= false then
                local counterNum = tonumber(counter);
                local sumTotal = tonumber(sn);

                -- 第一次进入，后续只要不超过internal就不会进入
                local sumTime = tonumber(durationTime) - tonumber(ttlTime);
                if sumTime > internal then
                    redis.call('HSET', activityId, requests, 1);
                    redis.call('HSET', activityId, timeWindow, ttlTime);
                elseif counterNum > sumTotal then
                    -- 并发值大于阀值
                    return -904;
                else
                    -- 设置时间间隔internal中的请求次数
                    redis.call('HSET', activityId, requests, counterNum + 1);
                end
            end
        end
    end

    -- 库存设计
    local balanceNum = redis.call('HGET', activityId, balanceNum);
    -- 剩余数为零或小于最小购买数
    if tonumber(balanceNum) == 0 or (balanceNum - minNum) < 0 then
        redis.call('HSET', activityId, activityState, "0");
        return balanceNum;
    end

    -- 抢购剩余数不足抢购失败
    if (balanceNum - minNum) >= 0 and (balanceNum - purchaseNum) < 0 then
        return -906;
    end
else
    ttlTime = expireTime;
end

-- 仅对IP进行限制购买
if restrictType == ipLimit then
    local ipExist = redis.call('HSETNX', useripKey, userip, ttlTime);
    if ipExist == 0 then
        return -901;
    else
        redis.call('EXPIRE', useripKey, ttlTime);
    end
elseif restrictType == idLimit then
    -- 限制用户id
    local idExist = redis.call('HSETNX', useridKey, userid, ttlTime);
    if idExist == 0 then
        return -902;
    else
        redis.call('EXPIRE', useridKey, ttlTime);
    end
elseif restrictType == ipAndIdLimit then
    -- 同时限制用户IP和Id码
    local ipExist = redis.call('HEXISTS', useripKey, userip);
    if (ipExist == 1) then
        return -903;
    end
    local idExist = redis.call('HEXISTS', useridKey, userid);
    if (idExist == 1) then
        return -903;
    end
    redis.call('HSET', useripKey, userip, ttlTime);
    redis.call('HSET', useridKey, userid, ttlTime);
    redis.call('EXPIRE', useridKey, ttlTime);
    redis.call('EXPIRE', useripKey, ttlTime);
end

-- 非限购型抢购直接返回OK
if activityType == 1 then
    return -100;
end

-- 存在订单号直接返回成功
local orderIdExist = redis.call('HSETNX', orderIdKey, orderIdValue, purchaseNum);
if orderIdExist == 0 then
    return -100;
else
    redis.call('EXPIRE', orderIdKey, ttlTime);
end

-- 减库存数
local lastNum = redis.call('HINCRBY', activityId, balanceNum, -purchaseNum);
if tonumber(lastNum) == 0 or (tonumber(lastNum) - minNum) < 0 then
    redis.call('HSET', activityId, activityState, "0");
    return lastNum;
end ;
return -100;