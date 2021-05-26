import React from 'react';
import AutocompleteCreatable from './AutocompleteCreatable';
import classnames from 'classnames';

const Card = props => {
  return (
    <div className={classnames("p-6 max-w-sm mx-auto rounded-xl shadow-md space-y-6 bg-opacity-20", props.color)}>
      <div className={classnames("text-xl font-medium text-gray-500")}>{props.name}</div>
      <AutocompleteCreatable/>
    </div>
  );
}

export default Card;
