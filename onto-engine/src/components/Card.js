import React from 'react';
import AutocompleteCreatable from './AutocompleteCreatable';

const Card = props => {
  return (
    <div className="p-6 max-w-sm mx-auto bg-white rounded-xl shadow-md space-y-6">
      <div className="text-xl font-medium text-blue-600">{props.name}</div>
      <AutocompleteCreatable/>
    </div>
  );
}

export default Card;
