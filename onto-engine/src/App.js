import './App.css';
import Card from './components/Card';

function App() {
  return (
    <>
      <div className="grid grid-rows-1 grid-flow-col gap-4">
        <div>
          <Card/>
        </div>
        <div>
          <Card/>
        </div>
        <div>
          <Card/>
        </div>
      </div>
    </>
  );
}

export default App;
