import './App.css';
import AutocompleteCreatable from './components/AutocompleteCreatable';
import ModalDialog from './components/ModalDialog';

function App() {
  return (
    <>
      <div className="p-6 max-w-sm mx-auto bg-white rounded-xl shadow-md space-y-6">
        <div className="text-xl font-medium text-blue-600">Subject</div>
        <input type="text" className="rounded-lg border-transparent flex-1 appearance-none border border-gray-300 w-full py-2 px-4 bg-white text-gray-700 placeholder-gray-400 shadow-sm text-base focus:outline-none focus:ring-2 focus:ring-purple-600 focus:border-transparent"/>
        <AutocompleteCreatable/>
        <ModalDialog/>
      </div>
    </>
  );
}

export default App;
