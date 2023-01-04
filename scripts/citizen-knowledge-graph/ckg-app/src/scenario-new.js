const canvas = document.querySelector("canvas");
const width = canvas.width;
const height = canvas.height;

const scene = new THREE.Scene();
const camera = new THREE.PerspectiveCamera(45, width / height, 1, 1000);
camera.position.set(0, 0, 10);
camera.lookAt(new THREE.Vector3(0, 0, 0));

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

buildLine([[0, 0, 0], [1, 0, 0]], "#000");
buildLine([[0, 0, 0], [-1, 0, 0]], "#fff");

const geometry = new THREE.PlaneGeometry(1, 5);
const material = new THREE.MeshBasicMaterial({color: 0xff0000 });
const plane = new THREE.Mesh(geometry, material);
plane.position.set(0, 0, 0);
scene.add(plane);

renderer.render(scene, camera);

canvas.addEventListener("click", event => {
  let interval = 50;
  let duration = 3000;
  let startPos = [0, 0, 10];
  let endPos = [0, 0, 1];
  let startLook = [0, 0, 0];
  let endLook = [0, 2, 0];

  let steps = duration / interval;
  let step = 0;
  let elapsed = 0;
  let pos = startPos;
  let look = startLook;

  const intervalId = setInterval(() => {
    elapsed += interval;

    pos = startPos.map((s, i) => s + (endPos[i] - s) * step / steps);
    look = startLook.map((s, i) => s + (endLook[i] - s) * step / steps);

    camera.position.set(pos[0], pos[1], pos[2]);
    camera.lookAt(new THREE.Vector3(look[0], look[1], look[2]));
    renderer.render(scene, camera);

    step ++;

    if (elapsed >= duration) clearInterval(intervalId);
  }, interval);
});
