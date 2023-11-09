program factorial ;
var a,b : integer ;

function calculo (x,y:integer; z:integer): integer;
begin
   calculo := x + y + z
end;

function fact( n : integer ) : integer ;
begin
   if n < 2 then
      fact := 1
   else
   begin
       write(n);
       fact := fact(n-1)*n
   end
end;

Begin
   read(a);
   write(fact(a))
end.
