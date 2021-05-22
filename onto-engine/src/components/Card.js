import AutocompleteCreatable from './AutocompleteCreatable';

export default function Card() {
  return (
    <div className="p-6 max-w-sm mx-auto bg-white rounded-xl shadow-md space-y-6">
      <div className="text-xl font-medium text-blue-600">Subject</div>
      <AutocompleteCreatable/>
    </div>
  );
}
