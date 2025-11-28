<h1 align="center">C++ OOP Design</h1>

<h1 id="table-of-contents">Table of Contents</h1>

- [1. Basics of Inheritance \& Class Structure](#1-basics-of-inheritance--class-structure)
- [2. Runtime Polymorphism \& V-Table](#2-runtime-polymorphism--v-table)
- [3. Resource Management \& Ownership](#3-resource-management--ownership)
- [4. Design Contracts \& Safety](#4-design-contracts--safety)
- [5. Advanced Inheritance (The Diamond Problem)](#5-advanced-inheritance-the-diamond-problem)
- [6. Example 1: Shape](#6-example-1-shape)
- [7. Example 2: File System](#7-example-2-file-system)
- [8. Example 3: Parking Lot](#8-example-3-parking-lot)

---

# 1. Basics of Inheritance & Class Structure
> **Core Concept:** Establishing relationships between classes ("Is-A") and managing object lifecycles.

## Inheritance Syntax & Access Specifiers
Syntax: `class Derived : [Access] Base { ... };`

The `[Access]` keyword (public/protected/private) determines **how the Base class's members are exposed** in the Derived class.

* **`public` Inheritance (Most Common)**:
    * **Meaning**: "Is-A" relationship.
    * **Effect**: Public members of Base remain **Public** in Derived. The outside world knows Derived is a Base.
    * *Example:* `class Car : public Vehicle` (A Car *is a* Vehicle).
* **`private` Inheritance (Default for `class`)**:
    * **Meaning**: "Implemented-in-terms-of" (similar to Composition).
    * **Effect**: Public/Protected members of Base become **Private** in Derived. The outside world *doesn't* know inheritance exists.
* **`protected` Inheritance**: Rare. Public members become Protected.

## Member Access Control (Inside Class)
* **`public`**: Visible to everyone.
* **`private`**: Visible **only** to the class itself (Derived classes cannot access).
* **`protected`**: Visible to the class **and its Derived classes**, but hidden from the outside world.

## Lifecycle: Construction & Destruction Order
When an object is created or destroyed, C++ follows a strict stack-like order:

1.  **Construction**: **Base First** $\to$ **Derived Last**.
    * *Why?* The Derived class relies on the Base class being initialized first (e.g., setting up vtables, initializing base members).
2.  **Destruction**: **Derived First** $\to$ **Base Last**.
    * *Why?* (Stack Unwinding) The Derived part must be cleaned up before the Base part it depends on is destroyed.

## Code Example
```cpp
class Base {
public:
    Base() { cout << "Base Constructed"; }
    // Destructor should be virtual in polymorphic base classes
    virtual ~Base() { cout << "Base Destroyed"; }
};

// 'public' keeps Base's public methods public in Derived
class Derived : public Base {
public:
    // Base() is implicitly called before Derived body executes
    Derived() { cout << "Derived Constructed"; }
    ~Derived() { cout << "Derived Destroyed"; }
};

int main() {
    Derived d;
    // Output Order:
    // 1. Base Constructed
    // 2. Derived Constructed
    // --- End of Scope ---
    // 3. Derived Destroyed
    // 4. Base Destroyed
}
```

[⬆️ Back to Top](#table-of-contents)
---

# 2. Runtime Polymorphism & V-Table
- **Core Concept:** The ability to call derived class methods through a base class pointer or reference at runtime.
- **Mechanism:** Implemented via **Virtual Table (vtable)** and **Virtual Pointer (vptr)**.

## The "Under the Hood" Mechanics
How does `basePtr->draw()` know which function to call?

1.  **`vtable` (Static Table per Class)**:
    * Generated for **ANY** class (Base OR Derived) that is **polymorphic**.
    * **Definition of Polymorphic:** A class that **declares** a `virtual` function, or **inherits** from a class that has `virtual` functions.
    * The compiler creates a **unique** static table for *each* of these classes, containing function pointers to the most specific implementations available to that class.

2.  **`vptr` (Hidden Pointer per Object)**:
    * Every object instance of a polymorphic class contains a hidden pointer (`vptr`).
    * **Initialization:** In the constructor, this pointer is set to point to the `vtable` of the **actual class being constructed** (e.g., a `Derived` object's `vptr` always points to `Derived::vtable`, even if assigned to a `Base*`).

## V-Table Construction Logic (Compile Time)
How does the compiler build the `Derived` class's vtable? It follows a **"Copy & Overwrite"** strategy:

1.  **Copy**: It starts by copying the layout and function pointers from the `Base` class's vtable.
2.  **Overwrite (Override)**: If the `Derived` class overrides a function (e.g., `func1`), the compiler updates that specific slot to point to the new implementation (`&Derived::func1`).
3.  **Inherit**: If a function is NOT overridden (e.g., `func2`), the slot remains unchanged, pointing to the original `Base` implementation (`&Base::func2`).
4.  **Append**: If `Derived` adds a **new** virtual function, it is added to the end of the table.

### Visualizing the Tables
Assume `Base` has `virtual f1()` and `virtual f2()`. `Derived` overrides `f1()` but not `f2()`.

| Slot | Base::vtable | Derived::vtable    | Status                                    |
| :--- | :----------- | :----------------- | :---------------------------------------- |
| 0    | `&Base::f1`  | **`&Derived::f1`** | **Overridden** (Polymorphism works here)  |
| 1    | `&Base::f2`  | `&Base::f2`        | **Inherited** (Reuse Base implementation) |

**Runtime Flow:**
1.  Program sees `ptr->func()`.
2.  It follows `ptr` to the object in memory.
3.  It reads the hidden `vptr` to find the `vtable` address.
4.  It looks up the function address in the `vtable`.
5.  It jumps to that address and executes the code.

## Code Example

```cpp
#include <iostream>
using namespace std;

class Base {
public:
    // 'virtual' tells compiler to generate vtable entry
    // You should ALWAYS add 'virtual' if the function is to be overriden
    virtual void func() { cout << "Base impl" << endl; }
};

class Derived : public Base {
public:
    // 'override' ensures signature matches Base's virtual function
    void func() override { cout << "Derived impl" << endl; } 
};

void execute(Base* ptr) {
    // Polymorphism happens here!
    // Compiler doesn't know if ptr is Base or Derived at compile time.
    // It generates code to look at ptr->vptr->vtable[index_of_func]
    ptr->func(); 
}

int main() {
    Base b;
    Derived d;
    
    execute(&b); // Prints "Base impl"
    execute(&d); // Prints "Derived impl" -> Dynamic Binding
}
```

## Additional Technical Details
* **Performance Overhead**: Virtual function calls are slightly slower than regular calls due to pointer indirection (dereferencing `vptr` then `vtable`).
* **Space Overhead**: Each object gets an extra pointer (`vptr`, usually 8 bytes on 64-bit systems), regardless of how many virtual functions it has.
* **Pure Virtual Function**: `virtual void f() = 0;`. Makes the class **Abstract** (cannot be instantiated). Used to define Interfaces.

[⬆️ Back to Top](#table-of-contents)
---

# 3. Resource Management & Ownership

- **Core Concept:** Managing object lifecycles and memory safety. In modern C++, this means **RAII** (Resource Acquisition Is Initialization) and **Virtual Destructors**.

## Virtual Destructor (The "Must-Have")
**The Golden Rule:** If a class is intended to be used as a Base class (polymorphically), it **MUST** have a `virtual` destructor.

### The Problem: Memory Leak
If the Base destructor is **not** virtual, `delete basePtr` only calls `~Base()`. The Derived part of the object remains in memory (leak).

```cpp
class Base {
public:
    // ⚠️ DANGER: Non-virtual destructor
    ~Base() { cout << "Base destroyed\n"; }
};

class Derived : public Base {
    int* data;
public:
    Derived() { data = new int[100]; }
    ~Derived() { 
        delete[] data; 
        cout << "Derived destroyed (Memory Freed)\n"; 
    }
};

void test() {
    Base* p = new Derived();
    delete p; 
    // Result: Only prints "Base destroyed". 
    // ~Derived() is NEVER called. 'data' array is LEAKED.
}
```

### The Fix
Add `virtual` to the Base destructor.

```cpp
class Base {
public:
    // ✅ Safe: Ensures ~Derived() is called first
    virtual ~Base() { cout << "Base destroyed\n"; }
};
```

## Smart Pointers (Modern C++)
**Header:** `<memory>`

In modern C++ (C++11+), manual `new` and `delete` are discouraged in OOD. We use Smart Pointers to express **Ownership**.

### 1. `std::unique_ptr` (Exclusive Ownership)
* **Semantics:** "I own this object alone. When I die, it dies."
* **OOD Mapping:** **Composition** (e.g., A `Car` owns an `Engine`).
* **Performance:** Zero overhead (same as raw pointer).
* **Copying:** **Not allowed** (Move-only).

```cpp
class Car {
    // Composition: Engine fits inside Car's lifecycle
    unique_ptr<Engine> engine; 
public:
    Car() : engine(make_unique<Engine>()) {}
};
```

### 2. `std::shared_ptr` (Shared Ownership)
* **Semantics:** "I share this object with others. It dies when the last owner dies."
* **OOD Mapping:** **Aggregation** (e.g., Multiple `User` objects belong to a `Group`, but users exist independently).
* **Performance:** Small overhead (Reference Counting).

```cpp
class User {
    string name;
};

class Group {
    // Aggregation: Users can exist in multiple groups
    vector<shared_ptr<User>> members;
};
```

[⬆️ Back to Top](#table-of-contents)
---

# 4. Design Contracts & Safety
> **Core Concept:** Writing code that explicitly communicates intent. This prevents misuse of your classes and allows the compiler to catch logic errors early.

## The "Virtual" Contract
In C++ Inheritance, the `virtual` keyword defines the nature of the relationship.

* **Virtual Function**: "I provide a default implementation, but you can customize it." (Interface + Default Implementation)
* **Pure Virtual Function (`= 0`)**: "I provide the interface, but YOU MUST implement it." (Interface Only)
* **Non-Virtual Function**: "This behavior is invariant. DO NOT change it." (Mandatory Implementation)

- **Golden Rule:** Never redefine an inherited **non-virtual** function. It breaks polymorphism and causes inconsistent behavior based on pointer type.

## Implementation Specifiers (`= 0` vs `= default`)
Defining the *nature* of a function's implementation.

* **Pure Virtual (`= 0`)**:
    * **Meaning**: "I have **NO** implementation. Derived classes **MUST** provide one."
    * **Consequence**: The class becomes **Abstract** (cannot be instantiated).
    * *Usage:* Defining Interfaces.

* **Defaulted (`= default`)** (C++11):
    * **Meaning**: "I want the compiler-generated **standard** implementation."
    * **Consequence**: The compiler generates the default version (e.g., member-wise copy/move).
    * *Usage:* Bringing back default constructors after defining custom ones, or defining a virtual destructor without writing an empty body.

### Quick Comparison
```cpp
class Base {
public:
    // Abstract Interface: Children MUST implement draw
    virtual void draw() = 0; 
    
    // Standard Behavior: Compiler generates standard destructor logic
    // (Crucial for polymorphic base classes)
    virtual ~Base() = default; 
};
```

## The `override` Keyword (C++11)
**Best Practice:** Always use `override` when you intend to rewrite a virtual function in a derived class.

### Why? (Safety)
If you make a typo (e.g., wrong parameter type or const-ness), the compiler will normally treat it as a **new, separate function** (Hiding), not an override.
* **With `override`**: Compiler throws an error: "You said you wanted to override, but no matching base function found."

```cpp
class Base {
public:
    virtual void foo(int x);
};

class Derived : public Base {
public:
    // Error! Base takes 'int', this takes 'float'.
    // Without 'override', this compiles as a totally new function.
    void foo(float x) override; 
};
```

## Const Correctness
**Definition:** Appending `const` to a member function.
**Syntax:** `int getArea() const { ... }`

### The Contract
- **Promise:** "Calling this function will NOT modify any member variables of this object."
- **Enforcement:** The compiler will throw an error if you try to assign to `this->member` inside.
- **Usage:** Getters, calculation methods, print methods.

> **Why it matters:** You can only call `const` methods on a `const` object reference. If you forget `const` on `getArea()`, you can't calculate the area of a read-only Shape!

## Access Specifiers (Encapsulation)
- **`public`**: The external interface. "Anyone can use this."
- **`protected`**: The family secret. "Only me and my children (derived classes) can use this." (Common for internal helpers in base classes).
- **`private`**: The implementation detail. "Only I can use this." (Children cannot access).

[⬆️ Back to Top](#table-of-contents)
---

# 5. Advanced Inheritance (The Diamond Problem)
> **Core Concept:** Handling complexities arising from **Multiple Inheritance**. The most famous scenario is the "Diamond Problem," where a class inherits from two classes that share a common base.

## The Scenario: The Diamond
Imagine a hierarchy shaped like a diamond:
* **`Asset`** (Root)
* **`Stock`** inherits from `Asset`
* **`Bond`** inherits from `Asset`
* **`ETF`** (Exchange Traded Fund) inherits from **BOTH** `Stock` and `Bond`

```cpp
class Asset { public: int id; };
class Stock : public Asset {};
class Bond  : public Asset {};
class ETF   : public Stock, public Bond {}; // Multiple Inheritance
```

## The Problem: Ambiguity & Redundancy
If you create an `ETF` object, it contains:
1.  A `Stock` part (which has an `Asset`).
2.  A `Bond` part (which *also* has an `Asset`).

**Consequences:**
* **Redundancy**: The `ETF` object has **TWO** copies of `id` (one from Stock, one from Bond).
* **Ambiguity**: If you call `etf.id`, the compiler errors out: "Which `id` do you mean? `Stock::id` or `Bond::id`?"

## The Solution: Virtual Inheritance
To fix this, the intermediate classes (`Stock` and `Bond`) must inherit from `Asset` **virtually**. This tells the compiler: "If any other class also inherits virtually from `Asset`, please merge them into a single shared instance."

## Code Example

```cpp
class Asset { 
public: 
    int id; 
};

// Note the 'virtual' keyword here
class Stock : virtual public Asset {};
class Bond  : virtual public Asset {};

// Now ETF only has ONE copy of Asset
class ETF : public Stock, public Bond {};

int main() {
    ETF myEtf;
    
    // Now valid! No ambiguity.
    // Takes the single shared 'id' from the virtual base.
    myEtf.id = 100; 
}
```

## Additional Technical Details
* **Memory Layout**: Virtual inheritance changes the object layout. It usually adds a pointer (similar to vptr) to track the offset of the shared base class in memory.
* **Initialization**: In virtual inheritance, the **most derived class** (`ETF`) is responsible for initializing the virtual base (`Asset`). The constructors of intermediate classes (`Stock`/`Bond`) effectively ignore the virtual base initialization.
* **Best Proctice:**: The "Java Interface" Style
C++ allows full Multiple Inheritance (inheriting data from multiple parents), which leads to the Diamond Problem. Java prevents this by allowing only multiple *Interface* inheritance. To avoid complexity, mimic Java in C++: Inherit from **at most one** Concrete Base Class (Implementation), and **multiple** Pure Abstract Base Classes (Interfaces), as the following.
    ```cpp
    // ✅ Safe Pattern: 1 Concrete Base + N Pure Interfaces
    class Duck : public Animal,        // Implementation (Data)
                public IFlyable,      // Interface (Behavior only)
                public ISwimmable {   // Interface (Behavior only)
        // ...
    };
    ```

[⬆️ Back to Top](#table-of-contents)
---

# 6. Example 1: Shape

## Problem Descirption
Design a simple **Graphics Drawing System** using C++ Object-Oriented Programming (OOD) principles.

The system should support the following:
1.  Define a generic **Shape** concept.
2.  Implement specific shapes: **Circle** (defined by radius) and **Rectangle** (defined by width and height).
3.  All shapes must provide the following behaviors:
    * Calculate their area (`getArea`).
    * Draw themselves to the console (`draw`).
4.  The client code should be able to store different types of shapes in a single container and operate on them **polymorphically** (i.e., without knowing their specific concrete types).
5.  Ensure proper memory management when deleting shapes via base class pointers.

---

## Key OOD Concepts & Implementation Details

This example demonstrates the fundamental mechanism of **Runtime Polymorphism** in C++.

### 1. Abstract Base Class & Pure Virtual Functions (`= 0`)
* **Syntax:** `virtual void draw() const = 0;`
* **Concept:** The `= 0` syntax declares a function as **Pure Virtual**.
* **Why:**
    * It makes `Shape` an **Abstract Class**, meaning you cannot create an instance of `Shape` (e.g., `new Shape()` is illegal).
    * It enforces a **Contract (Interface)**: Any concrete subclass (`Circle`, `Rectangle`) **MUST** implement these functions. If they don't, they remain abstract and cannot be instantiated.

### 2. Virtual Destructor (Crucial!)
* **Syntax:** `virtual ~Shape() {}`
* **Why:** This is the most critical safety feature in C++ inheritance.
    * When we `delete shapePtr` (where `shapePtr` is a `Shape*` pointing to a `Circle` object), the compiler looks at the static type of the pointer.
    * **Without `virtual`**: Only `~Shape()` is called. `~Circle()` is skipped, causing a **Memory Leak** (members of Circle are not cleaned up).
    * **With `virtual`**: The deletion happens dynamically. `~Circle()` is called first, followed by `~Shape()`.

### 3. Const Correctness (`func() const`)
* **Syntax:** `double getArea() const { ... }`
* **Why:** The `const` after the function parameter list indicates that this function **will not modify** any member variables of the object.
    * It allows these functions to be called on `const` objects.
    * It is a good engineering practice to mark "read-only" operations (like calculating area) as `const`.

### 4. The `override` Keyword
* **Syntax:** `void draw() const override { ... }`
* **Why:** (C++11 feature) It explicitly tells the compiler: "I intend to rewrite a virtual function from the base class".
    * **Safety**: If you make a typo in the function name or signature (e.g., `draw(int)` instead of `draw()`), the compiler will throw an error immediately, preventing bugs where a new function is created instead of overriding the existing one.

### 5. Runtime Polymorphism (vptr & vtable)
* **Mechanism**: How does `shape->draw()` know to call `Circle::draw()`?
    * Every object of a class with virtual functions contains a hidden pointer called **vptr**.
    * `vptr` points to a **vtable** (Virtual Method Table), which stores the addresses of the actual functions for that specific class.
    * At runtime, the program follows: `Object -> vptr -> vtable -> Actual Function`.

## Example Implementation

```cpp
#include <iostream>
#include <vector>
#include <cmath>

using namespace std;

// 1. Abstract Base Class (抽象基类)
// 相当于 Java 中的 Interface
class Shape {
public:
    // 【关键点 1】虚析构函数 (Virtual Destructor)
    // 如果不写 virtual，当 delete shapePtr 时，子类的析构函数不会被调用 -> 内存泄漏
    virtual ~Shape() {
        cout << "Shape destructor called" << endl;
    }

    // 【关键点 2】纯虚函数 (Pure Virtual Function)
    // = 0 表示这个类不能被实例化，强制子类必须实现这个函数
    virtual double getArea() const = 0;
    virtual void draw() const = 0;
};

// 2. Concrete Class (具体类)：圆形
class Circle : public Shape {
private:
    double radius;

public:
    Circle(double r) : radius(r) {}

    ~Circle() override { // C++11 override 关键字，防止拼写错误
        cout << "Circle destructor called" << endl;
    }

    // 实现基类的接口
    double getArea() const override {
        return 3.14159 * radius * radius;
    }

    void draw() const override {
        cout << "Drawing Circle with radius: " << radius << endl;
    }
};

// 3. Concrete Class (具体类)：矩形
class Rectangle : public Shape {
private:
    double width, height;

public:
    Rectangle(double w, double h) : width(w), height(h) {}

    ~Rectangle() override {
        cout << "Rectangle destructor called" << endl;
    }

    double getArea() const override {
        return width * height;
    }

    void draw() const override {
        cout << "Drawing Rectangle (" << width << " x " << height << ")" << endl;
    }
};

// 4. Client Code (使用多态)
int main() {
    // 使用基类指针容器，存储不同类型的子类对象
    vector<Shape*> shapes;
    
    shapes.push_back(new Circle(5.0));
    shapes.push_back(new Rectangle(4.0, 6.0));

    for (Shape* s : shapes) {
        // 【关键点 3】运行时多态 (Runtime Polymorphism)
        // 编译器会查找 vptr -> vtable，调用实际对象的 draw 方法
        s->draw(); 
        cout << "Area: " << s->getArea() << endl;
    }

    // 清理内存
    for (Shape* s : shapes) {
        delete s; // 这里会先调用子类析构，再调用基类析构
    }

    return 0;
}
```

[⬆️ Back to Top](#table-of-contents)
---

# 7. Example 2: File System

## Problem Description
Design an in-memory **File System** that supports the following:
1.  **Hierarchical Structure**: The system consists of **Files** (leaf nodes) and **Directories** (container nodes).
2.  **Nesting**: A Directory can contain Files and other sub-Directories.
3.  **Polymorphic Behavior**: Treat Files and Directories uniformly.
    * `getSize()`: For a File, return its data size. For a Directory, return the sum of sizes of all its contents (recursive).
    * `isDirectory()`: Identification helper.

## Key OOD Concepts & Pitfalls

### The Composite Pattern
This is the textbook definition of the **Composite Pattern**:
* **Component**: `FileSystemNode` (Abstract Base).
* **Leaf**: `File`.
* **Composite**: `Directory` (Contains a collection of `FileSystemNode*`).
* **Benefit**: The client code (e.g., calculating total size) looks identical whether it's dealing with a single file or a complex folder structure.

### Polymorphic Containers (`vector<Base*>`)
* **Pitfall**: You cannot store objects (`vector<FileSystemNode>`) because `FileSystemNode` is abstract, and even if it weren't, it would cause **Object Slicing**.
* **Solution**: You must store pointers: `vector<FileSystemNode*>`.
* **Modern C++**: In production, use `vector<unique_ptr<FileSystemNode>>` for automatic memory management. For interviews, raw pointers are acceptable if you handle the destructor correctly.

### Recursive Destruction (Memory Management)
* **Crucial Logic**: Since `Directory` holds pointers to children, it "owns" them.
* **Destructor**: The `~Directory()` destructor must iterate through its children and `delete` them. Without this, deleting the root node will orphan all children, causing a massive **Memory Leak**.

## Example Implementation
```cpp
#include <iostream>
#include <vector>
#include <string>
#include <algorithm>

using namespace std;

// 1. 抽象组件：FileSystemNode
class FileSystemNode {
protected:
    string name;

public:
    FileSystemNode(string n) : name(n) {}
    virtual ~FileSystemNode() = default;

    // 通用接口
    virtual int getSize() const = 0;
    virtual bool isDirectory() const = 0;
    
    // 打印路径结构（带缩进）
    virtual void print(int indent = 0) const {
        for (int i = 0; i < indent; ++i) cout << "  ";
        cout << name << " (" << getSize() << " bytes)" << endl;
    }
};

// 2. 叶子节点：File
class File : public FileSystemNode {
private:
    string content;

public:
    File(string n, string c) : FileSystemNode(n), content(c) {}

    int getSize() const override {
        return content.size();
    }

    bool isDirectory() const override {
        return false;
    }
};

// 3. 容器节点：Directory (包含其他 Node)
class Directory : public FileSystemNode {
private:
    // 【关键点】这里存的是基类指针，所以既能存 File 也能存 Directory
    vector<FileSystemNode*> children;

public:
    Directory(string n) : FileSystemNode(n) {}

    ~Directory() {
        for (auto child : children) {
            delete child; // 递归删除所有子节点
        }
    }

    void add(FileSystemNode* node) {
        children.push_back(node);
    }

    // 【关键点】递归计算大小
    int getSize() const override {
        int totalSize = 0;
        for (auto child : children) {
            totalSize += child->getSize(); // 多态调用
        }
        return totalSize;
    }

    bool isDirectory() const override {
        return true;
    }

    void print(int indent = 0) const override {
        FileSystemNode::print(indent); // 打印自己
        for (auto child : children) {
            child->print(indent + 1); // 递归打印孩子
        }
    }
};

// Client
int main() {
    Directory* root = new Directory("root");
    Directory* home = new Directory("home");
    Directory* user = new Directory("zacking");
    
    File* f1 = new File("notes.txt", "Hello World"); // size 11
    File* f2 = new File("config.xml", "<xml></xml>"); // size 11

    root->add(home);
    home->add(user);
    user->add(f1);
    user->add(f2);

    // 打印树状结构，并自动计算文件夹大小
    root->print();
    
    // 只要删除 root，整棵树都会被析构（因为 Directory 析构里处理了 children）
    delete root; 

    return 0;
}
```

[⬆️ Back to Top](#table-of-contents)
---

# 8. Example 3: Parking Lot

## Problem Description
Design a **Parking Lot** management system.
1.  **Capacity**: The parking lot has multiple **Parking Spots**.
2.  **Vehicle Types**: It must accommodate different types of vehicles: **Motorcycles**, **Cars**, and **Trucks**.
3.  **Spot Types**: Spots can be of different sizes (e.g., Compact, Large).
    * *Rules*: A Motorcycle can park anywhere. A Car needs at least a Compact spot. A Truck needs a Large spot (or multiple spots).
4.  **Operations**: The system should support `parkVehicle(Vehicle* v)` (finding an available spot) and `removeVehicle()`.

---

## Key OOD Concepts & Implementation Details

### Strongly Typed Enums (`enum class`)
* **Concept**: Instead of using `int` constants or C-style `enum`, C++11 introduces `enum class`.
* **Benefit**: Scoped and strongly typed. `VehicleType::CAR` cannot be implicitly converted to an integer or compared with `SpotType::COMPACT` without explicit casting, preventing logic errors.

### Operator Overloading (`operator<<`)
* **Concept**: Customizing how objects/enums are printed to streams (`cout`).
* **Application**: Since `enum class` does not print as a string by default, we overload the output stream operator to convert `VehicleType::CAR` into the string "Car" automatically. This creates very clean client code (`cout << vehicle->getType()`).

### Abstract Base Class for Polymorphism
* **Structure**: `Vehicle` is an abstract base class.
* **Polymorphism**: The `getSpotsNeeded()` function is pure virtual. This allows the `ParkingLot` to treat all vehicles uniformly. It asks "how much space do you need?" without caring if it's a Truck or a Car.

### Relationship Management (Association)
* **Logic**: The relationship between `Vehicle` and `ParkingSpot` is a "Has-A" (Aggregation) relationship during the parking duration.
* **Design**: The `ParkingSpot` holds a pointer `Vehicle*`. When the vehicle leaves, we simply set the pointer to `nullptr`, but we do **not** `delete` the vehicle memory (the driver still owns the car, the spot just held it).

## Example Implementation
```cpp
#include <iostream>
#include <vector>

using namespace std;

// 车型枚举
enum class VehicleType {
    MOTORCYCLE,
    CAR,
    TRUCK
};

// 重载 << 操作符，让 cout 知道怎么打印 Enum
ostream& operator<<(ostream& os, VehicleType type) {
    switch(type) {
        case VehicleType::MOTORCYCLE: os << "Motorcycle"; break;
        case VehicleType::CAR:        os << "Car"; break;
        case VehicleType::TRUCK:      os << "Truck"; break;
    }
    return os;
}

// 1. 车辆基类
class Vehicle {
protected:
    VehicleType type;
    string licensePlate;

public:
    Vehicle(VehicleType t, string plate) : type(t), licensePlate(plate) {}
    virtual ~Vehicle() = default;

    VehicleType getType() const { return type; }
    
    // 不同车需要的车位大小 (1个单位或多个单位)
    // 这是一个纯虚函数，或者是带有默认实现的虚函数
    virtual int getSpotsNeeded() const = 0;
};

class Car : public Vehicle {
public:
    Car(string plate) : Vehicle(VehicleType::CAR, plate) {}
    int getSpotsNeeded() const override { return 1; }
};

class Truck : public Vehicle {
public:
    Truck(string plate) : Vehicle(VehicleType::TRUCK, plate) {}
    int getSpotsNeeded() const override { return 5; } // 假设卡车需要5个连续车位
};

// 2. 车位类
class ParkingSpot {
private:
    VehicleType type; // 这个车位适合停什么车
    Vehicle* vehicle; // 当前停了谁 (nullptr 表示空)
    int spotNumber;

public:
    ParkingSpot(VehicleType t, int n) : type(t), spotNumber(n), vehicle(nullptr) {}

    bool isAvailable() const { return vehicle == nullptr; }

    // 检查某辆车能不能停这个位子
    bool canFit(Vehicle* v) {
        // 简化逻辑：车位类型必须匹配或者是大型车位兼容小型车
        return isAvailable() && (type == v->getType() || type == VehicleType::TRUCK);
    }

    void park(Vehicle* v) { vehicle = v; }
    void removeVehicle() { vehicle = nullptr; }
};

// 3. 停车场管理类 (Singleton 或者是 Manager)
class ParkingLot {
private:
    vector<ParkingSpot*> spots;

public:
    ParkingLot(int numSpots) {
        // 初始化一些车位
        for(int i=0; i<numSpots; ++i) {
            if (i < numSpots / 2) 
                spots.push_back(new ParkingSpot(VehicleType::CAR, i));
            else 
                spots.push_back(new ParkingSpot(VehicleType::TRUCK, i));
        }
    }

    ~ParkingLot() {
        for(auto s : spots) delete s;
    }

    // 核心业务逻辑：停车
    bool parkVehicle(Vehicle* v) {
        for (auto spot : spots) {
            if (spot->canFit(v)) {
                spot->park(v);
                cout << "Vehicle " << v->getType() << " parked at spot." << endl;
                return true;
            }
        }
        cout << "No spots available for this vehicle." << endl;
        return false;
    }
};

int main() {
    ParkingLot lot(10); // 10个车位
    
    Car* c1 = new Car("CA-123");
    Truck* t1 = new Truck("TR-999");
    
    lot.parkVehicle(c1);
    lot.parkVehicle(t1);
    
    delete c1;
    delete t1;
    return 0;
}
```

[⬆️ Back to Top](#table-of-contents)
---