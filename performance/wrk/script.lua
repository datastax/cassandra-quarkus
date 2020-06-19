counter = 0

request = function()
    wrk.format(method, path, headers, body)
    counter = counter + 1
    local json = '{"name":"product'
    json = json .. counter
    json = json .. '"},"description":"some_description"}'
    return wrk.format('POST', '/fruits', {["Content-Type"] = "application/json"}, json)
end