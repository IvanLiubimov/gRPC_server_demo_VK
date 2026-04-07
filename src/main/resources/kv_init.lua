
-- main space
box.cfg{listen=3301}
kv = box.schema.space.create('KV', {id = 513}) kv:format{{name='key', type='string'}, {name='value', type='varbinary', is_nullable=true}} kv:create_index('primary', {type='TREE', parts={1, 'STR'}})

box.schema.user.create('test', {password='test'})
box.schema.user.grant('test', 'read,write', 'space', 'KV')


-- test space
box.cfg{listen=3301}

kv = box.schema.space.create('KV_2', {id = 514}) kv:format{{name='key', type='string'}, {name='value', type='varbinary', is_nullable=true}} kv:create_index('primary', {type='TREE', parts={1, 'STR'}})

box.schema.user.create('test', {password='test'})
box.schema.user.grant('test', 'read,write', 'space', 'KV_2')