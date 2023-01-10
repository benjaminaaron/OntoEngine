import * as THREE from 'three';
import { FlyControls } from 'three/addons/controls/FlyControls.js';
import { FontLoader } from 'three/addons/loaders/FontLoader.js';
import { TextGeometry } from 'three/addons/geometries/TextGeometry.js';

const canvas = document.querySelector("canvas");
const width = canvas.width;
const height = canvas.height;

const scene = new THREE.Scene();
const camera = new THREE.PerspectiveCamera(45, width / height, 1, 1000);
const cameraStartPos = [0, 5, 14];
const cameraStartLookAt = [0, 5, 0];
const backgroundColor = "darkslategray";
const pathsColor = "silver";
const nodeBackgroundColor = "navajowhite";
const nodeTextColor = "black";

camera.position.set(cameraStartPos[0], cameraStartPos[1], cameraStartPos[2]);
camera.lookAt(new THREE.Vector3(cameraStartLookAt[0], cameraStartLookAt[1], cameraStartLookAt[2]));

const renderer = new THREE.WebGLRenderer({ canvas, antialias: true });
renderer.setClearColor(new THREE.Color(backgroundColor));

const controls = new FlyControls(camera, renderer.domElement);
// controls.listenToKeyEvents( window );
controls.movementSpeed = 4;
controls.rollSpeed = Math.PI / 24;
controls.dragToLook = true;

function buildLine(coords, color) {
  const mat = new THREE.LineBasicMaterial({ color: color });
  const points = [];
  for (let c of coords) points.push(new THREE.Vector3(c[0], c[1], c[2]));
  const geo = new THREE.BufferGeometry().setFromPoints(points);
  const line = new THREE.Line(geo, mat);
  scene.add(line);
}

function buildBezierShape(from, to) {
  // compensate for the two straight lines looking more bold than the curves
  const lineWidth = from[0] === to[0] ? 0.5 : 0.6;
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
  const m = new THREE.MeshBasicMaterial({ color: pathsColor });
  const mesh = new THREE.Mesh(g, m);
  scene.add(mesh);
}

function buildRoundedRect(pos, dim) {
  let widthHalf = dim[0] / 2;
  let heightHalf = dim[1] / 2;
  let rrSub = 0.4;
  let p1 = [- widthHalf, - heightHalf];
  let p12 = [- widthHalf + rrSub, - heightHalf];
  let p21 = [widthHalf - rrSub, - heightHalf];
  let p2 = [widthHalf, - heightHalf];
  let p23 = [widthHalf, - heightHalf + rrSub];
  let p32 = [widthHalf, heightHalf - rrSub];
  let p3 = [widthHalf, heightHalf];
  let p34 = [widthHalf - rrSub, heightHalf];
  let p43 = [- widthHalf + rrSub, heightHalf];
  let p4 = [- widthHalf, heightHalf];
  let p41 = [- widthHalf, heightHalf - rrSub];
  let p14 = [- widthHalf, - heightHalf + rrSub];

  let path = new THREE.Shape();
  // path.absellipse(0, 0, 1.2, 0.4, 0, Math.PI * 2, false, 0);
  path.moveTo(p12[0], p12[1]);
  path.lineTo(p21[0], p21[1]);
  path.quadraticCurveTo(p2[0], p2[1], p23[0], p23[1]);
  path.lineTo(p32[0], p32[1]);
  path.quadraticCurveTo(p3[0], p3[1], p34[0], p34[1]);
  path.lineTo(p43[0], p43[1]);
  path.quadraticCurveTo(p4[0], p4[1], p41[0], p41[1]);
  path.lineTo(p14[0], p14[1]);
  path.quadraticCurveTo(p1[0], p1[1], p12[0], p12[1]);

  let ellipse = new THREE.Mesh(
      new THREE.ShapeBufferGeometry(path),
      new THREE.MeshBasicMaterial({ color: nodeBackgroundColor })
  );
  ellipse.position.set(pos[0], pos[1], 0.0001);
  scene.add(ellipse);
}

function buildText(label, pos, callback) {
  let loader = new FontLoader();
  loader.load('../node_modules/three/examples/fonts/helvetiker_regular.typeface.json', function (font) {
    let textGeometry = new TextGeometry(label, {
      font: font,
      size: 0.3,
      height: 0.01,
    });
    let text = new THREE.Mesh(textGeometry,
        new THREE.MeshBasicMaterial({ color: nodeTextColor })
    );
    textGeometry.computeBoundingBox()
    let center = textGeometry.boundingBox.getCenter(new THREE.Vector3());
    text.position.set(pos[0] - center.x, pos[1] - center.y, 0.0002);
    scene.add(text);
    callback([textGeometry.boundingBox.max.x, textGeometry.boundingBox.max.y]);
  });
}

const graph = {
  root: { label: 'Wahl der Krankenversicherung', children: ['A', 'B'], pos: [0, 0], level: 0 },
  A: { label: 'Gesetzlich',  children: ['A1', 'A2', 'A3'] },
  B: { label: 'Privat',  children: ['B1', 'B2', 'B3'] },
  A1: { label: 'TK' },
  A2: { label: 'AOK' },
  A3: { label: '. . .' },
  B1: { label: 'Allianz' },
  B2: {  label: 'DKV' },
  B3: { label: '. . .' },
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

for (let key of Object.keys(graph)) {
  let node = graph[key];
  buildText(node.label, node.pos, bbox => {
    buildRoundedRect(node.pos, [bbox[0] + 0.6, bbox[1] + 0.6]);
  });
}

console.log(graph);

// buildLine([[0, 0, 0], [3, 3, 0]], "#000");

renderer.render(scene, camera);

let flyingToStreetView = false;
let startTime;
let duration = 3000;
let endPos = [0, 0, 1];
let endLook = [0, 10, 1];
let elapsed = 0;
let progress = 0;

function animate() {
  requestAnimationFrame(animate);
  controls.update(0.05);
  if (flyingToStreetView) {
    elapsed = performance.now() - startTime;
    progress = elapsed / duration;
    if (progress <= 1) {
      let pos = cameraStartPos.map((s, i) => s + (endPos[i] - s) * progress);
      let look = cameraStartLookAt.map((s, i) => s + (endLook[i] - s) * progress);
      camera.position.set(pos[0], pos[1], pos[2]);
      camera.lookAt(new THREE.Vector3(look[0], look[1], look[2]));
    } else {
      flyingToStreetView = false;
    }
  }
  renderer.render(scene, camera);
}

animate();

document.getElementById("devBtn").addEventListener("click", () => {
  flyingToStreetView = true;
  startTime = performance.now();
});
