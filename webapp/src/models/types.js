// @flow

export type DataBase = {
  id: number
};

export type DataUpdate<T: DataBase> = {
  operation: '+' | '-' | '=',
  item?: T,
  items?: T[]
};

export type JSONResponse<T> = {
  response?: any,
  data: {[key: string]: DataUpdate<T>}
};