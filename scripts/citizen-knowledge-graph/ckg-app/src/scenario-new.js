import * as THREE from 'three';

const canvas = document.querySelector("canvas");
const width = canvas.width;
const height = canvas.height;

const scene = new THREE.Scene();
const camera = new THREE.PerspectiveCamera(45, width / height, 1, 1000);
const cameraStartPos = [0, 5, 14];
const cameraStartLookAt = [0, 5, 0];

camera.position.set(cameraStartPos[0], cameraStartPos[1], cameraStartPos[2]);
camera.lookAt(new THREE.Vector3(cameraStartLookAt[0], cameraStartLookAt[1], cameraStartLookAt[2]));

const renderer = new THREE.WebGLRenderer({ canvas, antialias: true });
renderer.setClearColor(new THREE.Color("#ddd"));

function buildLine(coords, color) {
  const mat = new THREE.LineBasicMaterial({ color: color });
  const points = [];
  for (let c of coords) points.push(new THREE.Vector3(c[0], c[1], c[2]));
  const geo = new THREE.BufferGeometry().setFromPoints(points);
  const line = new THREE.Line(geo, mat);
  scene.add(line);
}

function buildBezierShape(from, to) {
  const lineWidth = 0.5;
  const p1 = [from[0] - lineWidth / 2, from[1]];
  const p2 = [from[0] + lineWidth / 2, from[1]];
  const p3 = [to[0] + lineWidth / 2, to[1]];
  const p4 = [to[0] - lineWidth / 2, to[1]];

  const cp1_23 = [p2[0], from[1] + (p3[1] - p2[1]) / 2];
  const cp2_23 = [p3[0], from[1] + (p3[1] - p2[1]) / 2];
  const cp1_41 = [p4[0], from[1] + (p4[1] - p1[1]) / 2];
  const cp2_41 = [p1[0], from[1] + (p4[1] - p1[1]) / 2];

  const shape = new THREE.Shape();

  shape.moveTo(p1[0], p1[1]);
  shape.lineTo(p2[0], p2[1]);
  shape.bezierCurveTo(cp1_23[0], cp1_23[1], cp2_23[0], cp2_23[1], p3[0], p3[1]);
  shape.lineTo(p4[0], p4[1]);
  shape.bezierCurveTo(cp1_41[0], cp1_41[1], cp2_41[0], cp2_41[1], p1[0], p1[1]);

  const g = new THREE.ShapeGeometry(shape);
  const m = new THREE.MeshBasicMaterial({ color: 0xff0000 });
  const mesh = new THREE.Mesh(g, m);
  scene.add(mesh);
}

const graph = {
  root: { label: 'Wahl der Krankenversicherung', children: ['A', 'B'], pos: [0, 0], level: 0 },
  A: { label: 'Gesetzlich',  children: ['A1', 'A2', 'A3'] },
  B: { label: 'Privat',  children: ['B1', 'B2', 'B3'] },
  A1: { label: 'TK' },
  A2: { label: 'AOK' },
  A3: { label: '...' },
  B1: { label: 'Allianz' },
  B2: {  label: 'DKV' },
  B3: { label: '...' },
};

const levelCounts = {};

function traverseEnrich(parent, level = 0) {
  if (!levelCounts[level]) levelCounts[level] = { total: 0, running: 0 };
  levelCounts[level].total += 1;
  level += 1;
  if (!parent.children) parent.children = [];
  for (let childKey of parent.children) {
    let child = graph[childKey];
    child.parent = parent;
    child.level = level;
    traverseEnrich(child, level);
  }
}

const yDist = 5;
const xDistBtwnNodes = 2.5;

function traverseDraw(parent) {
  for (let i = 0; i < parent.children.length; i++) {
    let child = graph[parent.children[i]];
    const from = parent.pos;
    const levelCount = levelCounts[child.level];
    const xTotalLevel = (levelCount.total - 1) * xDistBtwnNodes;
    const to = [- xTotalLevel / 2 + levelCount.running * xDistBtwnNodes, child.level * yDist];
    levelCount.running += 1;
    child.pos = to;
    buildBezierShape(from, to);
    traverseDraw(child);
  }
}

traverseEnrich(graph.root);
traverseDraw(graph.root);

console.log(graph);

// buildLine([[0, 0, 0], [3, 3, 0]], "#000");

renderer.render(scene, camera);

canvas.addEventListener("click", event => {
  let duration = 3000;
  let endPos = [0, 0, 1];
  let endLook = [0, 10, 1];
  let elapsed = 0;
  let progress = 0;
  let pos = cameraStartPos;
  let look = cameraStartLookAt;
  let animationId;
  const startTime = performance.now();

  function animate() {
    animationId = requestAnimationFrame(animate);
    elapsed = performance.now() - startTime;
    progress = elapsed / duration;

    pos = cameraStartPos.map((s, i) => s + (endPos[i] - s) * progress);
    look = cameraStartLookAt.map((s, i) => s + (endLook[i] - s) * progress);

    camera.position.set(pos[0], pos[1], pos[2]);
    camera.lookAt(new THREE.Vector3(look[0], look[1], look[2]));
    renderer.render(scene, camera);
  }

  animate();
  setTimeout(() => cancelAnimationFrame(animationId), duration);
});
