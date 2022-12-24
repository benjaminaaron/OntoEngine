let dropZone;
let callback;

function registerDropZone(_dropZone, _callback) {
  dropZone = _dropZone;
  dropZone.addEventListener('dragenter', handleDragEnter);
  dropZone.addEventListener('dragleave', handleDragLeave);
  dropZone.addEventListener('dragover', handleDragOver);
  dropZone.addEventListener('drop', handleDrop);
  callback = _callback;
}

function handleDragEnter(event) {
  event.stopPropagation();
  event.preventDefault();
  dropZone.classList.add('drag-over');
}

function handleDragLeave(event) {
  event.stopPropagation();
  event.preventDefault();
  dropZone.classList.remove('drag-over');
}

function handleDragOver(event) {
  event.stopPropagation();
  event.preventDefault();
}

function handleDrop(event) {
  event.stopPropagation();
  event.preventDefault();
  dropZone.classList.remove('drag-over');
  const files = event.dataTransfer.files;
  callback(files);
}
