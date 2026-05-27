type Props = {
  children: React.ReactNode;
};

export const PageTitle = ({ children }: Props) => (
  <h1 className="text-std-24B-150 text-solid-gray-900 mb-6">{children}</h1>
);
