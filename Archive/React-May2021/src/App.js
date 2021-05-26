import './App.css';
import Card from './components/Card';

function App() {
  return (
    <>
      <div className="flex justify-center py-24">
        <div className="w-[880px] grid grid-rows-1 grid-flow-col gap-8">
          <div>
            <Card
              name="Subject"
              color="bg-classColor"
            />
          </div>
          <div>
            <Card
              name="Predicate"
              color="bg-objectPropertyColor"
            />
          </div>
          <div>
            <Card
              name="Object"
              color="bg-classColor"
            />
          </div>
        </div>
      </div>
    </>
  );
}

export default App;
