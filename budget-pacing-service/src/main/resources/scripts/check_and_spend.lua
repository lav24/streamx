-- KEYS[1] = dedup key            (dedup:impression:{eventId})
-- KEYS[2] = daily spend key      (budget:spend:daily:{campaignId}:{date})
-- KEYS[3] = total spend key      (budget:spend:total:{campaignId})
-- KEYS[4] = daily cap key        (budget:cap:daily:{campaignId})
-- KEYS[5] = total cap key        (budget:cap:total:{campaignId})
-- ARGV[1] = dedup TTL (seconds)
-- ARGV[2] = impression cost (micro-dollars)

if redis.call('EXISTS', KEYS[1]) == 1 then
    return 'DUPLICATE'
end

local dailyCapRaw = redis.call('GET', KEYS[4])
local totalCapRaw = redis.call('GET', KEYS[5])

if dailyCapRaw == false or totalCapRaw == false then
    return 'UNKNOWN_CAMPAIGN'
end

local dailySpend = tonumber(redis.call('GET', KEYS[2]) or '0')
local totalSpend = tonumber(redis.call('GET', KEYS[3]) or '0')
local cost = tonumber(ARGV[2])
local dailyCap = tonumber(dailyCapRaw)
local totalCap = tonumber(totalCapRaw)

if dailySpend + cost > dailyCap then
    return 'DAILY_CAP_EXCEEDED'
end

if totalSpend + cost > totalCap then
    return 'TOTAL_CAP_EXCEEDED'
end

redis.call('SETEX', KEYS[1], ARGV[1], '1')
redis.call('INCRBY', KEYS[2], cost)
redis.call('INCRBY', KEYS[3], cost)

return 'OK'
