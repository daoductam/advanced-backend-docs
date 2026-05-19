import re

file_path = r'd:\backend_docs\caching-design\README.md'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Fix sequence diagram participants
content = re.sub(r'participant App as Application', r'participant App as "Application"', content)
content = re.sub(r'participant Cache as Cache Server \(Redis\)', r'participant Cache as "Cache Server (Redis)"', content)
content = re.sub(r'participant DB as Database', r'participant DB as "Database"', content)
content = re.sub(r'participant Cache as Cache Provider', r'participant Cache as "Cache Provider"', content)
content = re.sub(r'participant Cache as Cache Server', r'participant Cache as "Cache Server"', content)
content = re.sub(r'participant R1 as Request 1 \(Write\)', r'participant R1 as "Request 1 (Write)"', content)
content = re.sub(r'participant R2 as Request 2 \(Read\)', r'participant R2 as "Request 2 (Read)"', content)
content = re.sub(r'participant R1 as Request 1', r'participant R1 as "Request 1"', content)
content = re.sub(r'participant R2 as Request 2', r'participant R2 as "Request 2"', content)
content = re.sub(r'participant Cache as Cache', r'participant Cache as "Cache"', content)
content = re.sub(r'participant DB as DB', r'participant DB as "Database"', content)

# Fix actor to participant
content = re.sub(r'actor Client', r'participant Client', content)

# Fix graph node labels
content = re.sub(r'Cache\[Cache ❌ Down / Keys Expired\]', r'Cache["Cache ❌ Down / Keys Expired"]', content)
content = re.sub(r'Cache\[Cache ❌ k1 expired\]', r'Cache["Cache ❌ k1 expired"]', content)
content = re.sub(r'Cache\[Cache ❌ k1 not found\]', r'Cache["Cache ❌ k1 not found"]', content)
content = re.sub(r'DB\[\(Database ❌ k1 not found\)\]', r'DB[("Database ❌ k1 not found")]', content)
content = re.sub(r'Node1\[Cache Node 01 <br/> k34, k88 ❌ Sập\]', r'Node1["Cache Node 01 <br/> k34, k88 ❌ Sập"]', content)
content = re.sub(r'Node2\[Cache Node 02\]', r'Node2["Cache Node 02"]', content)
content = re.sub(r'Node3\[Cache Node 03\]', r'Node3["Cache Node 03"]', content)

# Fix thick link
content = re.sub(r'App -- "90% Traffic \(k34, k88\)" ==> Node1', r'App == "90% Traffic (k34, k88)" ==> Node1', content)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print('Mermaid diagrams fixed!')
