import urllib.request
import json
import os

# Change to the directory where the JSON file is
json_file = os.path.join(os.path.dirname(__file__), '..', 'frontend', 'DATOS', 'roadmap_angular_mvp.json')

# Get the first roadmap ID from the file
with open(json_file) as f:
    data = json.load(f)
    test_id = data['roadmaps'][0]['id'] if data['roadmaps'] else None

if test_id:
    print(f"Testing with roadmap ID: {test_id}\n")
    
    update_data = {
        'title': 'Test Roadmap Updated',
        'description': 'Now with ejes and iniciativas',
        'ejes_estrategicos': [
            {'id': 'EJE1', 'nombre': 'Transformación Digital', 'descripcion': 'Modernizar sistemas', 'color': '#667eea'},
            {'id': 'EJE2', 'nombre': 'Eficiencia', 'descripcion': 'Optimizar procesos', 'color': '#48bb78'}
        ],
        'iniciativas': [
            {'id': 'INIT1', 'nombre': 'API REST', 'eje': 'EJE1', 'tipo': 'Feature', 'expediente': 'EXP001', 
             'inicio': '2026-T1', 'fin': '2026-T2', 'objetivo': 'Crear APIs REST', 'impacto_principal': 'Alta',
             'usuarios_afectados': 'Todos', 'dependencias': [], 'certeza': 'comprometido'}
        ]
    }
    
    body = json.dumps(update_data).encode()
    url = f'http://localhost:8081/api/roadmaps/{test_id}'
    req = urllib.request.Request(url, data=body, headers={'Content-Type': 'application/json'}, method='PUT')
    res = urllib.request.urlopen(req)
    result = json.loads(res.read().decode())
    
    print('✓ SUCCESS! Roadmap updated with:')
    print(f'  - Title: {result["title"]}')
    print(f'  - Ejes: {len(result["ejes_estrategicos"])} ejes')
    print(f'  - Iniciativas: {len(result["iniciativas"])} iniciativas\n')
    
    print("Ejes estratégicos created:")
    for eje in result['ejes_estrategicos']:
        print(f'  ✓ {eje["nombre"]} (ID: {eje["id"]}, Color: {eje["color"]})')
    
    print("\nIniciativas created:")
    for init in result['iniciativas']:
        print(f'  ✓ {init["nombre"]} (Eje: {init["eje"]}, Certeza: {init["certeza"]})')
else:
    print("No roadmaps found!")
