program prueba ;{ Archivo Prueba C&I: EJ16K.PAS }
var
  a,b:integer;
function algo(x, y:integer): integer;
 begin
    x:= 10;
    y:= 10; 
    begin 
      x := x + 1;
      begin
        y := x - 1
      end;
      x := x + 1
    end; 
 end;
begin
  a:=9;
  algo( a );
end.

