import './App.css';
import Card from './components/Card';

function App() {
  return (
    <>
      <div className="flex justify-center py-24">
        <div className="w-[880px] grid grid-rows-1 grid-flow-col gap-8">
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
      </div>
    </>
  );
}

export default App;
