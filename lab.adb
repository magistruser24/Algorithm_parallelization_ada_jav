with Ada.Text_IO; use Ada.Text_IO;
with Ada.Integer_Text_IO; use Ada.Integer_Text_IO;
with Ada.Containers; use Ada.Containers;

procedure Parallel_Array_Sum is
   type Integer_Array is array (Positive range <>) of Integer;
   type Integer_Array_Access is access Integer_Array;

   task type Partial_Sum_Task (Input_Array : Integer_Array_Access;
                               Start_Index : Positive;
                               Partial_Result : out Integer);

   task body Partial_Sum_Task is
      Sum : Integer := 0;
   begin
      for I in Input_Array'Range loop
         if I = Start_Index or else I = Input_Array'Last - Start_Index + 1 then
            Sum := Sum + Input_Array(I);
         end if;
      end loop;

      Partial_Result := Sum;
   end Partial_Sum_Task;

   procedure Parallel_Array_Sum is
      Array_Size : constant Positive := 6; -- adjust as needed
      Task_Count : constant Positive := Array_Size / 2;
      Original_Array : Integer_Array (1 .. Array_Size) := (1, 2, 3, 4, 5, 6);
      Partial_Sums : array (1 .. Task_Count) of Integer;
      Total_Sum : Integer := 0;

      Tasks : array (1 .. Task_Count) of Partial_Sum_Task;

   begin
      for I in 1 .. Task_Count loop
         Tasks(I) :=
           (Input_Array => new Integer_Array'(Original_Array),
            Start_Index => I,
            Partial_Result => Partial_Sums(I));
      end loop;

      for T of Tasks loop
         accept T.Partial_Result do
            Total_Sum := Total_Sum + T.Partial_Result;
         end accept;
      end loop;

      Put_Line ("Final result: " & Total_Sum'Image);
   end Parallel_Array_Sum;

begin
   Parallel_Array_Sum;
end Parallel_Array_Sum;
